<?php
/**
 * Clear all admin notifications for a shop
 * POST Parameters: shop_id (required)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$shop_id = isset($_POST['shop_id']) ? intval($_POST['shop_id']) : 0;

if ($shop_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'Shop ID is required']);
    exit;
}

try {
    $table_check = $conn->query("SHOW TABLES LIKE 'admin_notifications'");
    if ($table_check->rowCount() == 0) {
        echo json_encode(['status' => true, 'message' => 'No notifications to clear']);
        exit;
    }
    
    $stmt = $conn->prepare("DELETE FROM admin_notifications WHERE shop_id = ?");
    $stmt->execute([$shop_id]);
    
    $deleted = $stmt->rowCount();
    
    echo json_encode([
        'status' => true,
        'message' => $deleted > 0 ? "$deleted notifications cleared" : 'No notifications to clear',
        'deleted_count' => $deleted
    ]);
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
