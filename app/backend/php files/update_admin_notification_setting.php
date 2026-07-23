<?php
/**
 * Update admin/shop notification setting
 * POST Parameters:
 *   - shop_id (required)
 *   - notifications_enabled (required - 1 for enabled, 0 for disabled)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$shop_id = isset($_POST['shop_id']) ? intval($_POST['shop_id']) : 0;
$notifications_enabled = isset($_POST['notifications_enabled']) ? intval($_POST['notifications_enabled']) : -1;

if ($shop_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'Shop ID is required']);
    exit;
}

if ($notifications_enabled < 0 || $notifications_enabled > 1) {
    echo json_encode(['status' => false, 'message' => 'notifications_enabled must be 0 or 1']);
    exit;
}

try {
    // Check if notifications_enabled column exists, add if not
    $column_check = $conn->query("SHOW COLUMNS FROM shops LIKE 'notifications_enabled'");
    if ($column_check->rowCount() == 0) {
        $conn->exec("ALTER TABLE shops ADD COLUMN notifications_enabled TINYINT(1) DEFAULT 1");
    }
    
    $stmt = $conn->prepare("UPDATE shops SET notifications_enabled = ? WHERE shop_id = ?");
    $stmt->execute([$notifications_enabled, $shop_id]);
    
    echo json_encode([
        'status' => true,
        'message' => 'Notification setting updated',
        'notifications_enabled' => $notifications_enabled === 1
    ]);
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
