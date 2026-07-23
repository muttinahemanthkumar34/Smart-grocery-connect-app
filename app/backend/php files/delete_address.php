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

if ($address_id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Address ID is required"
    ]);
    exit;
}

try {
    $stmt = $conn->prepare("DELETE FROM user_addresses WHERE address_id = ?");
    $stmt->execute([$address_id]);

    if ($stmt->rowCount() > 0) {
        echo json_encode([
            "success" => true,
            "message" => "Address deleted successfully"
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Address not found"
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to delete address: " . $e->getMessage()
    ]);
}
?>
