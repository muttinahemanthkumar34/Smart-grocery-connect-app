<?php
/**
 * Clear all notifications for a user
 * POST Parameters: user_id (required)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'User ID is required']);
    exit;
}

try {
    // Check if notifications table exists
    $table_check = $conn->query("SHOW TABLES LIKE 'notifications'");
    if ($table_check->rowCount() == 0) {
        echo json_encode(['status' => true, 'message' => 'No notifications to clear']);
        exit;
    }
    
    // Delete all notifications for this user
    $stmt = $conn->prepare("DELETE FROM notifications WHERE user_id = ?");
    $stmt->execute([$user_id]);
    
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
