<?php
header("Content-Type: application/json");
require_once("config/db.php");

/* Required fields */
$required = [
    'admin_id',
    'shop_name',
    'category',
    'shop_address',
    'city',
    'pincode',
    'opening_time',
    'closing_time'
];

foreach ($required as $field) {
    if (!isset($_POST[$field]) || $_POST[$field] === '') {
        echo json_encode([
            "status" => false,
            "message" => "Missing field: $field"
        ]);
        exit;
    }
}

/* Assign values */
$admin_id = $_POST['admin_id'];
$shop_name = $_POST['shop_name'];
$category = $_POST['category'];
$shop_address = $_POST['shop_address'];
$landmark = $_POST['landmark'] ?? null;
$city = $_POST['city'];
$pincode = $_POST['pincode'];
$opening_time = $_POST['opening_time'];
$closing_time = $_POST['closing_time'];
$delivery_available = $_POST['delivery_available'] ?? 0;

/* Insert shop */
try {
    $stmt = $conn->prepare(
        "INSERT INTO shops 
        (admin_id, shop_name, category, shop_address, landmark, city, pincode,
         opening_time, closing_time, delivery_available)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    );

    $stmt->execute([
        $admin_id,
        $shop_name,
        $category,
        $shop_address,
        $landmark,
        $city,
        $pincode,
        $opening_time,
        $closing_time,
        $delivery_available
    ]);

    echo json_encode([
        "status" => true,
        "message" => "Shop added successfully"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to add shop"
    ]);
}
