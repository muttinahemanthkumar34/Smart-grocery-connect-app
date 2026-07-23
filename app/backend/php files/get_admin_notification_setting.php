<?php
/**
 * Get admin/shop notification setting
 * GET Parameters: shop_id (required)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$shop_id = isset($_GET['shop_id']) ? intval($_GET['shop_id']) : 0;

if ($shop_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'Shop ID is required']);
    exit;
}

try {
    // Check if notifications_enabled column exists in shops table
    $column_check = $conn->query("SHOW COLUMNS FROM shops LIKE 'notifications_enabled'");
    if ($column_check->rowCount() == 0) {
        // Column doesn't exist, add it and return default true
        $conn->exec("ALTER TABLE shops ADD COLUMN notifications_enabled TINYINT(1) DEFAULT 1");
        echo json_encode([
            'status' => true,
            'notifications_enabled' => true
        ]);
        exit;
    }
    
    $stmt = $conn->prepare("SELECT notifications_enabled FROM shops WHERE shop_id = ?");
    $stmt->execute([$shop_id]);
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $enabled = isset($row['notifications_enabled']) ? (intval($row['notifications_enabled']) === 1) : true;
        echo json_encode([
            'status' => true,
            'notifications_enabled' => $enabled
        ]);
    } else {
        echo json_encode(['status' => false, 'message' => 'Shop not found']);
    }
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
