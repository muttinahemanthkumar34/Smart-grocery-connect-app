<?php
header("Content-Type: application/json");
require_once("config/db.php");

$response = array();

if (!isset($_GET['user_id']) || $_GET['user_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "User ID is required"
    ]);
    exit;
}

$user_id = intval($_GET['user_id']);

try {
    $stmt = $conn->prepare(
        "SELECT address_id, user_id, label, address_line, city, pincode, is_default
         FROM user_addresses
         WHERE user_id = ?
         ORDER BY is_default DESC, created_at DESC"
    );
    $stmt->execute([$user_id]);
    $addresses = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => true,
        "addresses" => $addresses
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch addresses"
    ]);
}
?>
