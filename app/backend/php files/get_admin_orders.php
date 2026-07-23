<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (!isset($_GET['shop_id']) || $_GET['shop_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "shop_id is required"
    ]);
    exit;
}

$shop_id = intval($_GET['shop_id']);

try {
    // Get all orders for this shop with user info
    // Using LEFT JOIN to handle cases where user might be deleted
    $stmt = $conn->prepare(
        "SELECT
            o.order_id,
            COALESCE(u.full_name, 'Unknown Customer') AS user_name,
            COALESCE(u.phone, '') AS user_phone,
            o.order_type,
            o.total_amount,
            o.order_status,
            o.created_at
         FROM orders o
         LEFT JOIN users u ON o.user_id = u.user_id
         WHERE o.shop_id = ?
         ORDER BY o.created_at DESC"
    );

    $stmt->execute([$shop_id]);
    $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Get order items for each order
    $itemStmt = $conn->prepare(
        "SELECT 
            p.product_name,
            oi.quantity,
            oi.price
         FROM order_items oi
         JOIN products p ON oi.product_id = p.product_id
         WHERE oi.order_id = ?"
    );

    foreach ($orders as &$order) {
        // Default status to PLACED if empty/null
        if (empty($order['order_status'])) {
            $order['order_status'] = 'PLACED';
        }
        
        $itemStmt->execute([$order['order_id']]);
        $order['items'] = $itemStmt->fetchAll(PDO::FETCH_ASSOC);
    }

    echo json_encode([
        "status" => true,
        "orders" => $orders,
        "count" => count($orders)
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch admin orders: " . $e->getMessage()
    ]);
}
