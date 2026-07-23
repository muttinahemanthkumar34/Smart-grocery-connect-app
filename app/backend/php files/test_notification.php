<?php
/**
 * Test script to verify notifications setup and create a test notification
 * DELETE THIS FILE AFTER TESTING
 * 
 * Usage: http://localhost/grocery_connect_api/test_notification.php?user_id=1
 */
header("Content-Type: application/json");
require_once("config/db.php");

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'Provide user_id in URL: ?user_id=1']);
    exit;
}

$result = [];

try {
    // Check if notifications table exists
    $table_check = $conn->query("SHOW TABLES LIKE 'notifications'");
    $result['notifications_table_exists'] = $table_check->rowCount() > 0;
    
    // Create table if not exists
    if (!$result['notifications_table_exists']) {
        $conn->exec("CREATE TABLE notifications (
            id INT PRIMARY KEY AUTO_INCREMENT,
            user_id INT NOT NULL,
            order_id INT NULL,
            title VARCHAR(100) NOT NULL,
            message VARCHAR(255) NOT NULL,
            is_read TINYINT(1) DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )");
        $result['notifications_table_created'] = true;
    }
    
    // Check if notifications_enabled column exists in users
    $column_check = $conn->query("SHOW COLUMNS FROM users LIKE 'notifications_enabled'");
    $result['notifications_enabled_column_exists'] = $column_check->rowCount() > 0;
    
    // Add column if not exists
    if (!$result['notifications_enabled_column_exists']) {
        $conn->exec("ALTER TABLE users ADD COLUMN notifications_enabled TINYINT(1) DEFAULT 1");
        $result['notifications_enabled_column_created'] = true;
    }
    
    // Check user's notification setting
    $userStmt = $conn->prepare("SELECT user_id, full_name, notifications_enabled FROM users WHERE user_id = ?");
    $userStmt->execute([$user_id]);
    $user = $userStmt->fetch(PDO::FETCH_ASSOC);
    $result['user'] = $user;
    
    // Count existing notifications
    $countStmt = $conn->prepare("SELECT COUNT(*) as count FROM notifications WHERE user_id = ?");
    $countStmt->execute([$user_id]);
    $count = $countStmt->fetch(PDO::FETCH_ASSOC);
    $result['existing_notifications_count'] = intval($count['count']);
    
    // Create a test notification
    $title = "Test Notification";
    $message = "This is a test notification created at " . date('Y-m-d H:i:s');
    
    $insertStmt = $conn->prepare("INSERT INTO notifications (user_id, order_id, title, message) VALUES (?, NULL, ?, ?)");
    $insertStmt->execute([$user_id, $title, $message]);
    $result['test_notification_created'] = true;
    $result['test_notification_id'] = $conn->lastInsertId();
    
    $result['status'] = true;
    $result['message'] = 'Setup verified and test notification created! Check your app now.';
    
} catch (Exception $e) {
    $result['status'] = false;
    $result['error'] = $e->getMessage();
}

echo json_encode($result, JSON_PRETTY_PRINT);
?>
