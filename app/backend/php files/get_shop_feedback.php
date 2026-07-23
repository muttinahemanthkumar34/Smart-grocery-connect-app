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

$shop_id = $_GET['shop_id'];

try {
    $stmt = $conn->prepare(
        "SELECT
            u.full_name AS user_name,
            f.rating,
            f.comments,
            f.created_at
         FROM feedback f
         JOIN users u ON f.user_id = u.user_id
         WHERE f.shop_id = ?
         ORDER BY f.created_at DESC"
    );

    $stmt->execute([$shop_id]);
    $feedbacks = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => true,
        "feedbacks" => $feedbacks
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch feedback"
    ]);
}
