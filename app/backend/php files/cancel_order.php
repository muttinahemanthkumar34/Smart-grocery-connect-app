<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (empty($_POST['order_id'])) {
    echo json_encode([
        "status" => false,
        "message" => "order_id is required"
    ]);
    exit;
}

$order_id = $_POST['order_id'];

try {
    $conn->beginTransaction();

    /* Get order items */
    $stmt = $conn->prepare(
        "SELECT product_id, quantity
         FROM order_items
         WHERE order_id = ?"
    );
    $stmt->execute([$order_id]);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (!$items) {
        throw new Exception("Invalid order");
    }

    /* Restore stock */
    $restoreStmt = $conn->prepare(
        "UPDATE products
         SET stock_quantity = stock_quantity + ?
         WHERE product_id = ?"
    );

    foreach ($items as $item) {
        $restoreStmt->execute([
            $item['quantity'],
            $item['product_id']
        ]);
    }

    /* Update order status */
    $updateOrderStmt = $conn->prepare(
        "UPDATE orders
         SET order_status = 'CANCELLED'
         WHERE order_id = ?"
    );
    $updateOrderStmt->execute([$order_id]);

    $conn->commit();
    
    // Get order details for notification
    try {
        $orderStmt = $conn->prepare("SELECT shop_id, total_amount FROM orders WHERE order_id = ?");
        $orderStmt->execute([$order_id]);
        $order = $orderStmt->fetch(PDO::FETCH_ASSOC);
        
        if ($order) {
            $shop_id = $order['shop_id'];
            $total = $order['total_amount'];
            
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
                
                $order_number = "#GRC-" . str_pad($order_id, 4, "0", STR_PAD_LEFT);
                $title = "Order Cancelled";
                $message = "Order $order_number (₹" . number_format($total, 2) . ") has been cancelled by customer.";
                
                $notifStmt = $conn->prepare("INSERT INTO admin_notifications (shop_id, order_id, title, message) VALUES (?, ?, ?, ?)");
                $notifStmt->execute([$shop_id, $order_id, $title, $message]);
            }
        }
    } catch (Exception $notifError) {
        error_log("Admin notification error: " . $notifError->getMessage());
    }

    echo json_encode([
        "status" => true,
        "message" => "Order cancelled and stock restored"
    ]);

} catch (Exception $e) {
    $conn->rollBack();
    echo json_encode([
        "status" => false,
        "message" => "Failed to cancel order"
    ]);
}
