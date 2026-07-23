<?php
/**
 * Get notifications for a user
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
    // Check if notifications table exists
    $table_check = $conn->query("SHOW TABLES LIKE 'notifications'");
    if ($table_check->rowCount() == 0) {
        // Return empty list if table doesn't exist yet
        echo json_encode([
            'status' => true,
            'unread_count' => 0,
            'notifications' => []
        ]);
        exit;
    }
    
    // Get all notifications for the user
    $stmt = $conn->prepare("
        SELECT n.id, n.order_id, n.title, n.message, n.is_read, n.created_at,
               o.order_status
        FROM notifications n
        LEFT JOIN orders o ON n.order_id = o.order_id
        WHERE n.user_id = ?
        ORDER BY n.created_at DESC
        LIMIT 50
    ");
    $stmt->execute([$user_id]);
    
    $notifications = [];
    $unread_count = 0;
    
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $notifications[] = [
            'id' => intval($row['id']),
            'order_id' => $row['order_id'] ? intval($row['order_id']) : null,
            'title' => $row['title'],
            'message' => $row['message'],
            'is_read' => intval($row['is_read']) === 1,
            'created_at' => $row['created_at'],
            'order_status' => $row['order_status'] ?? null
        ];
        
        if (intval($row['is_read']) === 0) {
            $unread_count++;
        }
    }
    
    echo json_encode([
        'status' => true,
        'unread_count' => $unread_count,
        'notifications' => $notifications
    ]);
    
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
