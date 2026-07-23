<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once("config/db.php");

$response = array();

if (!isset($_GET['shop_id']) || $_GET['shop_id'] === '') {
    $response['status'] = false;
    $response['message'] = "shop_id is required";
    echo json_encode($response);
    exit;
}

$shop_id = intval($_GET['shop_id']);

try {
    // Ensure upi_id column exists
    try {
        $conn->exec("ALTER TABLE shops ADD COLUMN upi_id VARCHAR(100) NULL");
    } catch (Exception $e) {
        // Column may already exist
    }
    
    $stmt = $conn->prepare("SELECT upi_id FROM shops WHERE shop_id = ?");
    $stmt->execute([$shop_id]);
    $shop = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($shop) {
        $response['status'] = true;
        $response['upi_id'] = $shop['upi_id'] ?? "";
    } else {
        $response['status'] = false;
        $response['message'] = "Shop not found";
    }
} catch (PDOException $e) {
    $response['status'] = false;
    $response['message'] = "Database error: " . $e->getMessage();
}

echo json_encode($response);
?>
