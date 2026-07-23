<?php
/**
 * Mark admin notification(s) as read
 * POST Parameters: 
 *   - shop_id (required)
 *   - notification_id (optional - if provided, marks specific notification)
 *   - mark_all (optional - if true, marks all notifications as read)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$shop_id = isset($_POST['shop_id']) ? intval($_POST['shop_id']) : 0;
$notification_id = isset($_POST['notification_id']) ? intval($_POST['notification_id']) : 0;
$mark_all = isset($_POST['mark_all']) && $_POST['mark_all'] === 'true';

if ($shop_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'Shop ID is required']);
    exit;
}

try {
    $table_check = $conn->query("SHOW TABLES LIKE 'admin_notifications'");
    if ($table_check->rowCount() == 0) {
        echo json_encode(['status' => true, 'message' => 'No notifications to mark']);
        exit;
    }
    
    if ($mark_all) {
        $stmt = $conn->prepare("UPDATE admin_notifications SET is_read = 1 WHERE shop_id = ?");
        $stmt->execute([$shop_id]);
    } else if ($notification_id > 0) {
        $stmt = $conn->prepare("UPDATE admin_notifications SET is_read = 1 WHERE id = ? AND shop_id = ?");
        $stmt->execute([$notification_id, $shop_id]);
    } else {
        echo json_encode(['status' => false, 'message' => 'Either notification_id or mark_all is required']);
        exit;
    }
    
    echo json_encode([
        'status' => true,
        'message' => $mark_all ? 'All notifications marked as read' : 'Notification marked as read'
    ]);
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
