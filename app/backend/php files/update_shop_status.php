<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Validate required fields
if (!isset($_POST['shop_id']) || $_POST['shop_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "shop_id is required"
    ]);
    exit;
}

if (!isset($_POST['field']) || $_POST['field'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "field is required (is_online or delivery_available)"
    ]);
    exit;
}

if (!isset($_POST['value'])) {
    echo json_encode([
        "status" => false,
        "message" => "value is required (0 or 1)"
    ]);
    exit;
}

$shop_id = intval($_POST['shop_id']);
$field = $_POST['field'];
$value = intval($_POST['value']);

// Validate field name to prevent SQL injection
$allowed_fields = ['is_online', 'delivery_available'];
if (!in_array($field, $allowed_fields)) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid field. Allowed: is_online, delivery_available"
    ]);
    exit;
}

// Validate value
if ($value !== 0 && $value !== 1) {
    echo json_encode([
        "status" => false,
        "message" => "Value must be 0 or 1"
    ]);
    exit;
}

try {
    $stmt = $conn->prepare("UPDATE shops SET $field = ? WHERE shop_id = ?");
    $stmt->execute([$value, $shop_id]);

    if ($stmt->rowCount() > 0) {
        echo json_encode([
            "status" => true,
            "message" => ucfirst(str_replace('_', ' ', $field)) . " updated successfully"
        ]);
    } else {
        echo json_encode([
            "status" => false,
            "message" => "Shop not found or no changes made"
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to update status: " . $e->getMessage()
    ]);
}
