<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (!isset($_GET['user_id']) || $_GET['user_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "user_id is required"
    ]);
    exit;
}

$user_id = $_GET['user_id'];

try {
    $stmt = $conn->prepare(
        "SELECT 
            o.order_id,
            o.shop_id,
            s.shop_name,
            o.order_type,
            o.total_amount,
            o.order_status,
            o.created_at,
            CASE WHEN f.feedback_id IS NOT NULL THEN 1 ELSE 0 END as feedback_submitted
         FROM orders o
         JOIN shops s ON o.shop_id = s.shop_id
         LEFT JOIN feedback f ON o.order_id = f.order_id
         WHERE o.user_id = ?
         ORDER BY o.created_at DESC"
    );

    $stmt->execute([$user_id]);
    $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => true,
        "orders" => $orders
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch user orders"
    ]);
}
