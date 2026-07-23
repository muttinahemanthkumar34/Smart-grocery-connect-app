<?php
header("Content-Type: application/json");
require_once("config/db.php");

/* Get user's default address city */
if (!isset($_GET['user_id']) || $_GET['user_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "User ID is required"
    ]);
    exit;
}

$user_id = $_GET['user_id'];

try {
    $stmt = $conn->prepare(
        "SELECT city, pincode, address_line
         FROM user_addresses
         WHERE user_id = ? AND is_default = 1
         LIMIT 1"
    );

    $stmt->execute([$user_id]);
    $address = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($address) {
        echo json_encode([
            "status" => true,
            "city" => $address['city'],
            "pincode" => $address['pincode'],
            "address" => $address['address_line']
        ]);
    } else {
        // No default address, try to get any address
        $stmt2 = $conn->prepare(
            "SELECT city, pincode, address_line
             FROM user_addresses
             WHERE user_id = ?
             ORDER BY created_at DESC
             LIMIT 1"
        );
        $stmt2->execute([$user_id]);
        $anyAddress = $stmt2->fetch(PDO::FETCH_ASSOC);
        
        if ($anyAddress) {
            echo json_encode([
                "status" => true,
                "city" => $anyAddress['city'],
                "pincode" => $anyAddress['pincode'],
                "address" => $anyAddress['address_line']
            ]);
        } else {
            echo json_encode([
                "status" => false,
                "message" => "No address found for user"
            ]);
        }
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch address"
    ]);
}
