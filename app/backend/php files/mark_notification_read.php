<?php
/**
 * Mark notification(s) as read
 * POST Parameters: 
 *   - user_id (required)
 *   - notification_id (optional - if provided, marks specific notification)
 *   - mark_all (optional - if true, marks all notifications as read)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
$notification_id = isset($_POST['notification_id']) ? intval($_POST['notification_id']) : 0;
$mark_all = isset($_POST['mark_all']) && $_POST['mark_all'] === 'true';

if ($user_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'User ID is required']);
    exit;
}

try {
    // Check if notifications table exists
    $table_check = $conn->query("SHOW TABLES LIKE 'notifications'");
    if ($table_check->rowCount() == 0) {
        echo json_encode(['status' => true, 'message' => 'No notifications to mark']);
        exit;
    }
    
    if ($mark_all) {
        // Mark all notifications as read for this user
        $stmt = $conn->prepare("UPDATE notifications SET is_read = 1 WHERE user_id = ?");
        $stmt->execute([$user_id]);
    } else if ($notification_id > 0) {
        // Mark specific notification as read
        $stmt = $conn->prepare("UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?");
        $stmt->execute([$notification_id, $user_id]);
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
