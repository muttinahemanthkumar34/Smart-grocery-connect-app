<?php
header("Content-Type: application/json");
require_once("config/db.php");

$response = array();

if ($_SERVER['REQUEST_METHOD'] != 'POST') {
    echo json_encode([
        "success" => false,
        "message" => "Invalid request method"
    ]);
    exit;
}

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
$label = isset($_POST['label']) ? trim($_POST['label']) : '';
$address_line = isset($_POST['address_line']) ? trim($_POST['address_line']) : '';
$city = isset($_POST['city']) ? trim($_POST['city']) : '';
$pincode = isset($_POST['pincode']) ? trim($_POST['pincode']) : '';
$is_default = isset($_POST['is_default']) ? intval($_POST['is_default']) : 0;

if ($user_id <= 0 || empty($label) || empty($address_line) || empty($city) || empty($pincode)) {
    echo json_encode([
        "success" => false,
        "message" => "All fields are required"
    ]);
    exit;
}

try {
    // If this is set as default, unset other defaults first
    if ($is_default == 1) {
        $stmt = $conn->prepare("UPDATE user_addresses SET is_default = 0 WHERE user_id = ?");
        $stmt->execute([$user_id]);
    }

    $stmt = $conn->prepare(
        "INSERT INTO user_addresses (user_id, label, address_line, city, pincode, is_default)
         VALUES (?, ?, ?, ?, ?, ?)"
    );
    $stmt->execute([$user_id, $label, $address_line, $city, $pincode, $is_default]);

    echo json_encode([
        "success" => true,
        "message" => "Address added successfully",
        "address_id" => $conn->lastInsertId()
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to add address: " . $e->getMessage()
    ]);
}
?>
