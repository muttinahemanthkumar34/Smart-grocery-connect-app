<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (
    empty($_POST['order_id']) ||
    empty($_POST['order_status'])
) {
    echo json_encode([
        "status" => false,
        "message" => "order_id and order_status are required"
    ]);
    exit;
}

$order_id = intval($_POST['order_id']);
$order_status = strtoupper(trim($_POST['order_status']));

/* Allowed statuses */
$allowed_status = [
    'PLACED',
    'PACKING', 
    'READY', 
    'OUT_FOR_DELIVERY',
    'DELIVERED', 
    'CANCELLED'
];

if (!in_array($order_status, $allowed_status)) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid order status: " . $order_status
    ]);
    exit;
}

try {
    $conn->beginTransaction();

    // Update order status
    if ($order_status === 'DELIVERED') {
        // Set delivered_at timestamp when order is delivered
        $stmt = $conn->prepare(
            "UPDATE orders
             SET order_status = ?, delivered_at = NOW()
             WHERE order_id = ?"
        );
    } else {
        $stmt = $conn->prepare(
            "UPDATE orders
             SET order_status = ?
             WHERE order_id = ?"
        );
    }
    $stmt->execute([$order_status, $order_id]);

    // If order is cancelled, restore product stock
    if ($order_status === 'CANCELLED') {
        // Get order items
        $itemsStmt = $conn->prepare(
            "SELECT product_id, quantity FROM order_items WHERE order_id = ?"
        );
        $itemsStmt->execute([$order_id]);
        $items = $itemsStmt->fetchAll(PDO::FETCH_ASSOC);

        // Restore stock for each item (using correct column name)
        foreach ($items as $item) {
            $updateStockStmt = $conn->prepare(
                "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?"
            );
            $updateStockStmt->execute([$item['quantity'], $item['product_id']]);
        }
    }

    $conn->commit();
    
    // Create notification for the user
    try {
        // Get user_id for this order
        $userStmt = $conn->prepare("SELECT user_id FROM orders WHERE order_id = ?");
        $userStmt->execute([$order_id]);
        $orderRow = $userStmt->fetch(PDO::FETCH_ASSOC);
        
        if ($orderRow) {
            $user_id = intval($orderRow['user_id']);
            
            // Check if user has notifications enabled
            $notifCheck = $conn->query("SHOW COLUMNS FROM users LIKE 'notifications_enabled'");
            $notificationsEnabled = true;
            
            if ($notifCheck->rowCount() > 0) {
                $notifStmt = $conn->prepare("SELECT notifications_enabled FROM users WHERE user_id = ?");
                $notifStmt->execute([$user_id]);
                $notifRow = $notifStmt->fetch(PDO::FETCH_ASSOC);
                if ($notifRow) {
                    $notificationsEnabled = intval($notifRow['notifications_enabled']) === 1;
                }
            }
            
            if ($notificationsEnabled) {
                // Create notifications table if not exists
                $conn->exec("CREATE TABLE IF NOT EXISTS notifications (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    order_id INT NULL,
                    title VARCHAR(100) NOT NULL,
                    message VARCHAR(255) NOT NULL,
                    is_read TINYINT(1) DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )");
                
                // Build notification message
                $title = 'Order Update';
                $message = "Your order #$order_id status has been updated.";
                
                switch ($order_status) {
                    case 'PLACED':
                        $title = 'Order Placed!';
                        $message = "Your order #$order_id has been placed successfully.";
                        break;
                    case 'PACKING':
                        $title = 'Order Being Packed';
                        $message = "Your order #$order_id is now being packed.";
                        break;
                    case 'READY':
                        $title = 'Order Ready!';
                        $message = "Your order #$order_id is ready for pickup/delivery.";
                        break;
                    case 'OUT_FOR_DELIVERY':
                        $title = 'Out for Delivery';
                        $message = "Your order #$order_id is out for delivery.";
                        break;
                    case 'DELIVERED':
                        $title = 'Order Completed!';
                        $message = "Your order #$order_id has been delivered. Thank you!";
                        break;
                    case 'CANCELLED':
                        $title = 'Order Cancelled';
                        $message = "Your order #$order_id has been cancelled.";
                        break;
                }
                
                // Insert notification
                $insertStmt = $conn->prepare("INSERT INTO notifications (user_id, order_id, title, message) VALUES (?, ?, ?, ?)");
                $insertStmt->execute([$user_id, $order_id, $title, $message]);
                
                // Send FCM Push Notification (works even when app is closed)
                require_once("fcm_helper.php");
                error_log("Looking for FCM tokens for user_id: $user_id");
                $fcm_tokens = FCMNotification::getUserTokens($conn, $user_id);
                error_log("Found " . count($fcm_tokens) . " FCM tokens for user");
                foreach ($fcm_tokens as $token) {
                    $result = FCMNotification::sendOrderNotification($token, $title, $message, $order_id);
                    error_log("FCM send result for user: " . json_encode($result));
                }
            }
        }
    } catch (Exception $notifError) {
        // Don't fail the main request if notification fails
        error_log("Notification error: " . $notifError->getMessage());
    }

    echo json_encode([
        "status" => true,
        "message" => "Order status updated successfully",
        "new_status" => $order_status
    ]);

} catch (Exception $e) {
    $conn->rollBack();
    echo json_encode([
        "status" => false,
        "message" => "Failed to update order status: " . $e->getMessage()
    ]);
}
