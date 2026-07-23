<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Check required fields - using isset instead of empty for numeric fields
if (
    !isset($_POST['order_id']) || $_POST['order_id'] === '' ||
    !isset($_POST['user_id']) || $_POST['user_id'] === '' ||
    !isset($_POST['shop_id']) || $_POST['shop_id'] === '' ||
    !isset($_POST['rating']) || $_POST['rating'] === ''
) {
    echo json_encode([
        "status" => false,
        "message" => "Required fields are missing"
    ]);
    exit;
}

$order_id = intval($_POST['order_id']);
$user_id = intval($_POST['user_id']);
$shop_id = intval($_POST['shop_id']);
$rating = intval($_POST['rating']);
$comments = isset($_POST['comments']) ? $_POST['comments'] : null;

// Validate IDs
if ($order_id <= 0 || $user_id <= 0 || $shop_id <= 0) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid order, user, or shop ID"
    ]);
    exit;
}

if ($rating < 1 || $rating > 5) {
    echo json_encode([
        "status" => false,
        "message" => "Rating must be between 1 and 5"
    ]);
    exit;
}

try {
    /* Check order status */
    $check = $conn->prepare(
        "SELECT order_status
         FROM orders
         WHERE order_id = ? AND user_id = ?"
    );
    $check->execute([$order_id, $user_id]);
    $order = $check->fetch(PDO::FETCH_ASSOC);

    if (!$order || $order['order_status'] !== 'DELIVERED') {
        echo json_encode([
            "status" => false,
            "message" => "Feedback allowed only after delivery"
        ]);
        exit;
    }

    /* Prevent duplicate feedback */
    $exists = $conn->prepare(
        "SELECT feedback_id FROM feedback WHERE order_id = ?"
    );
    $exists->execute([$order_id]);

    if ($exists->rowCount() > 0) {
        echo json_encode([
            "status" => false,
            "message" => "Feedback already submitted"
        ]);
        exit;
    }

    /* Insert feedback */
    $stmt = $conn->prepare(
        "INSERT INTO feedback (order_id, user_id, shop_id, rating, comments)
         VALUES (?, ?, ?, ?, ?)"
    );
    $stmt->execute([
        $order_id,
        $user_id,
        $shop_id,
        $rating,
        $comments
    ]);

    echo json_encode([
        "status" => true,
        "message" => "Feedback submitted successfully"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to submit feedback: " . $e->getMessage()
    ]);
}
