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

$address_id = isset($_POST['address_id']) ? intval($_POST['address_id']) : 0;
$label = isset($_POST['label']) ? trim($_POST['label']) : '';
$address_line = isset($_POST['address_line']) ? trim($_POST['address_line']) : '';
$city = isset($_POST['city']) ? trim($_POST['city']) : '';
$pincode = isset($_POST['pincode']) ? trim($_POST['pincode']) : '';
$is_default = isset($_POST['is_default']) ? intval($_POST['is_default']) : 0;

if ($address_id <= 0 || empty($label) || empty($address_line) || empty($city) || empty($pincode)) {
    echo json_encode([
        "success" => false,
        "message" => "All fields are required"
    ]);
    exit;
}

try {
    // Get user_id for this address
    $stmt = $conn->prepare("SELECT user_id FROM user_addresses WHERE address_id = ?");
    $stmt->execute([$address_id]);
    $address = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$address) {
        echo json_encode([
            "success" => false,
            "message" => "Address not found"
        ]);
        exit;
    }

    $user_id = $address['user_id'];

    // If this is set as default, unset other defaults first
    if ($is_default == 1) {
        $stmt = $conn->prepare("UPDATE user_addresses SET is_default = 0 WHERE user_id = ?");
        $stmt->execute([$user_id]);
    }

    $stmt = $conn->prepare(
        "UPDATE user_addresses 
         SET label = ?, address_line = ?, city = ?, pincode = ?, is_default = ?
         WHERE address_id = ?"
    );
    $stmt->execute([$label, $address_line, $city, $pincode, $is_default, $address_id]);

    echo json_encode([
        "success" => true,
        "message" => "Address updated successfully"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to update address: " . $e->getMessage()
    ]);
}
?>
