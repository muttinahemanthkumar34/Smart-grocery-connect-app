<?php
/**
 * Get user notification setting
 * GET Parameters: user_id (required)
 */
header("Content-Type: application/json");
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(['status' => false, 'message' => 'User ID is required']);
    exit;
}

try {
    // Check if notifications_enabled column exists
    $column_check = $conn->query("SHOW COLUMNS FROM users LIKE 'notifications_enabled'");
    if ($column_check->rowCount() == 0) {
        // Column doesn't exist, return default true
        echo json_encode([
            'status' => true,
            'notifications_enabled' => true
        ]);
        exit;
    }
    
    $stmt = $conn->prepare("SELECT notifications_enabled FROM users WHERE user_id = ?");
    $stmt->execute([$user_id]);
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $enabled = isset($row['notifications_enabled']) ? (intval($row['notifications_enabled']) === 1) : true;
        echo json_encode([
            'status' => true,
            'notifications_enabled' => $enabled
        ]);
    } else {
        echo json_encode(['status' => false, 'message' => 'User not found']);
    }
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
