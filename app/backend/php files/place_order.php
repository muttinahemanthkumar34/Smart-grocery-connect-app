<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (
    empty($_POST['user_id']) ||
    empty($_POST['shop_id']) ||
    empty($_POST['order_type']) ||
    empty($_POST['items'])
) {
    echo json_encode([
        "status" => false,
        "message" => "All fields are required"
    ]);
    exit;
}

$user_id = $_POST['user_id'];
$shop_id = $_POST['shop_id'];
$order_type = $_POST['order_type'];
$payment_method = isset($_POST['payment_method']) ? $_POST['payment_method'] : 'COD';
$items = json_decode($_POST['items'], true);

if (!is_array($items) || count($items) === 0) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid items data"
    ]);
    exit;
}

try {
    // Ensure payment_method column exists (outside transaction)
    try {
        $conn->exec("ALTER TABLE orders ADD COLUMN payment_method VARCHAR(20) DEFAULT 'COD'");
    } catch (Exception $e) {
        // Column may already exist
    }

    $conn->beginTransaction();

    /* Calculate total amount */
    $total_amount = 0;
    foreach ($items as $item) {
        $total_amount += $item['price'] * $item['quantity'];
    }

    /* Insert order */
    $stmt = $conn->prepare(
        "INSERT INTO orders (user_id, shop_id, order_type, total_amount, payment_method)
         VALUES (?, ?, ?, ?, ?)"
    );
    $stmt->execute([$user_id, $shop_id, $order_type, $total_amount, $payment_method]);

    $order_id = $conn->lastInsertId();

    /* Prepare statements */
    $checkStockStmt = $conn->prepare(
        "SELECT stock_quantity FROM products WHERE product_id = ? FOR UPDATE"
    );

    $updateStockStmt = $conn->prepare(
        "UPDATE products
         SET stock_quantity = stock_quantity - ?
         WHERE product_id = ?"
    );

    $insertItemStmt = $conn->prepare(
        "INSERT INTO order_items (order_id, product_id, quantity, price)
         VALUES (?, ?, ?, ?)"
    );

    /* Process each item */
    foreach ($items as $item) {

        $product_id = $item['product_id'];
        $quantity = $item['quantity'];
        $price = $item['price'];

        /* Check stock */
        $checkStockStmt->execute([$product_id]);
        $product = $checkStockStmt->fetch(PDO::FETCH_ASSOC);

        if (!$product || $product['stock_quantity'] < $quantity) {
            throw new Exception("Insufficient stock for product ID: $product_id");
        }

        /* Reduce stock */
        $updateStockStmt->execute([$quantity, $product_id]);

        /* Insert order item */
        $insertItemStmt->execute([
            $order_id,
            $product_id,
            $quantity,
            $price
        ]);
    }

    $conn->commit();

    // Generate formatted order number
    $order_number = "#GRC-" . str_pad($order_id, 4, "0", STR_PAD_LEFT);
    
    // Create admin notification for new order
    try {
        // Check if admin has notifications enabled
        $notifCheckStmt = $conn->prepare("SELECT notifications_enabled FROM shops WHERE shop_id = ?");
        $notifCheckStmt->execute([$shop_id]);
        $shopSettings = $notifCheckStmt->fetch(PDO::FETCH_ASSOC);
        
        $notificationsEnabled = true; // Default to true if column doesn't exist
        if ($shopSettings && isset($shopSettings['notifications_enabled'])) {
            $notificationsEnabled = intval($shopSettings['notifications_enabled']) === 1;
        }
        
        if ($notificationsEnabled) {
            // Create admin_notifications table if not exists
            $conn->exec("CREATE TABLE IF NOT EXISTS admin_notifications (
                id INT PRIMARY KEY AUTO_INCREMENT,
                shop_id INT NOT NULL,
                order_id INT NULL,
                title VARCHAR(100) NOT NULL,
                message VARCHAR(255) NOT NULL,
                is_read TINYINT(1) DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )");
            
            $title = "New Order Received!";
            $message = "Order $order_number has been placed. Total: ₹" . number_format($total_amount, 2);
            
            $notifStmt = $conn->prepare("INSERT INTO admin_notifications (shop_id, order_id, title, message) VALUES (?, ?, ?, ?)");
            $notifStmt->execute([$shop_id, $order_id, $title, $message]);
            
            // Send FCM Push Notification to shop admin (works even when app is closed)
            require_once("fcm_helper.php");
            $admin_tokens = FCMNotification::getShopTokens($conn, $shop_id);
            foreach ($admin_tokens as $token) {
                FCMNotification::sendOrderNotification($token, $title, $message, $order_id);
            }
        }
        
        // Check for low/empty stock and create notifications
        $LOW_STOCK_THRESHOLD = 10;
        
        // Add product_id column to admin_notifications if not exists
        try {
            $conn->exec("ALTER TABLE admin_notifications ADD COLUMN product_id INT NULL");
        } catch (Exception $e) {
            // Column may already exist
        }
        
        foreach ($items as $item) {
            $product_id = $item['product_id'];
            
            // Get current stock and product name
            $stockCheckStmt = $conn->prepare("SELECT product_name, stock_quantity FROM products WHERE product_id = ?");
            $stockCheckStmt->execute([$product_id]);
            $productInfo = $stockCheckStmt->fetch(PDO::FETCH_ASSOC);
            
            if ($productInfo) {
                $currentStock = intval($productInfo['stock_quantity']);
                $productName = $productInfo['product_name'];
                
                if ($currentStock <= 0) {
                    // Out of stock notification
                    $stockTitle = "⚠️ Out of Stock!";
                    $stockMessage = "$productName is now OUT OF STOCK. Please restock immediately.";
                    
                    $stockNotifStmt = $conn->prepare("INSERT INTO admin_notifications (shop_id, product_id, title, message) VALUES (?, ?, ?, ?)");
                    $stockNotifStmt->execute([$shop_id, $product_id, $stockTitle, $stockMessage]);
                    
                } else if ($currentStock <= $LOW_STOCK_THRESHOLD) {
                    // Low stock notification
                    $stockTitle = "⚠️ Low Stock Alert";
                    $stockMessage = "$productName has only $currentStock units left. Consider restocking soon.";
                    
                    $stockNotifStmt = $conn->prepare("INSERT INTO admin_notifications (shop_id, product_id, title, message) VALUES (?, ?, ?, ?)");
                    $stockNotifStmt->execute([$shop_id, $product_id, $stockTitle, $stockMessage]);
                }
            }
        }
    } catch (Exception $notifError) {
        // Don't fail the main request if notification fails
        error_log("Admin notification error: " . $notifError->getMessage());
    }

    echo json_encode([
        "status" => true,
        "order_id" => $order_id,
        "order_number" => $order_number,
        "message" => "Order placed successfully"
    ]);

} catch (Exception $e) {
    $conn->rollBack();
    echo json_encode([
        "status" => false,
        "message" => $e->getMessage()
    ]);
}
