<?php
/**
 * Update user notification setting (enable/disable)
 * POST Parameters:
 *   - user_id (required)
 *   - notifications_enabled (required - 1 for enabled, 0 for disabled)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
$notifications_enabled = isset($_POST['notifications_enabled']) ? intval($_POST['notifications_enabled']) : -1;

if ($user_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'User ID is required']);
    exit;
}

if ($notifications_enabled < 0 || $notifications_enabled > 1) {
    echo json_encode(['status' => false, 'message' => 'notifications_enabled must be 0 or 1']);
    exit;
}

try {
    // Check if notifications_enabled column exists, add if not
    $column_check = $conn->query("SHOW COLUMNS FROM users LIKE 'notifications_enabled'");
    if ($column_check->rowCount() == 0) {
        $conn->exec("ALTER TABLE users ADD COLUMN notifications_enabled TINYINT(1) DEFAULT 1");
    }
    
    $stmt = $conn->prepare("UPDATE users SET notifications_enabled = ? WHERE user_id = ?");
    $stmt->execute([$notifications_enabled, $user_id]);
    
    echo json_encode([
        'status' => true,
        'message' => 'Notification setting updated',
        'notifications_enabled' => $notifications_enabled === 1
    ]);
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
