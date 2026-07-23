<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (!isset($_GET['order_id']) || $_GET['order_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "order_id is required"
    ]);
    exit;
}

$order_id = intval($_GET['order_id']);

try {
    /* Fetch order info with user and shop details */
    $orderStmt = $conn->prepare(
        "SELECT 
            o.order_id,
            s.shop_name,
            s.shop_phone,
            u.full_name AS user_name,
            u.phone AS user_phone,
            o.order_type,
            o.total_amount,
            o.order_status,
            o.created_at,
            COALESCE(o.payment_method, 'COD') AS payment_method
         FROM orders o
         JOIN shops s ON o.shop_id = s.shop_id
         JOIN users u ON o.user_id = u.user_id
         WHERE o.order_id = ?"
    );
    $orderStmt->execute([$order_id]);
    $order = $orderStmt->fetch(PDO::FETCH_ASSOC);

    if (!$order) {
        echo json_encode([
            "status" => false,
            "message" => "Order not found"
        ]);
        exit;
    }

    /* Fetch order items with product image */
    $itemsStmt = $conn->prepare(
        "SELECT 
            p.product_name,
            p.product_image,
            oi.quantity,
            oi.price
         FROM order_items oi
         JOIN products p ON oi.product_id = p.product_id
         WHERE oi.order_id = ?"
    );
    $itemsStmt->execute([$order_id]);
    $items = $itemsStmt->fetchAll(PDO::FETCH_ASSOC);

    // Include items in order object for easier parsing
    $order['items'] = $items;

    echo json_encode([
        "status" => true,
        "order" => $order
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch order details: " . $e->getMessage()
    ]);
}

