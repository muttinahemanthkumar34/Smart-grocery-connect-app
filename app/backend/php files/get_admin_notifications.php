<?php
/**
 * Get notifications for a shop (admin side)
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
    // Check if admin_notifications table exists
    $table_check = $conn->query("SHOW TABLES LIKE 'admin_notifications'");
    if ($table_check->rowCount() == 0) {
        // Create table if not exists
        $conn->exec("CREATE TABLE admin_notifications (
            id INT PRIMARY KEY AUTO_INCREMENT,
            shop_id INT NOT NULL,
            order_id INT NULL,
            title VARCHAR(100) NOT NULL,
            message VARCHAR(255) NOT NULL,
            is_read TINYINT(1) DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )");
        
        echo json_encode([
            'status' => true,
            'unread_count' => 0,
            'notifications' => []
        ]);
        exit;
    }
    
    // Get all notifications for the shop
    $stmt = $conn->prepare("
        SELECT n.id, n.order_id, n.product_id, n.title, n.message, n.is_read, n.created_at,
               o.order_status
        FROM admin_notifications n
        LEFT JOIN orders o ON n.order_id = o.order_id
        WHERE n.shop_id = ?
        ORDER BY n.created_at DESC
        LIMIT 50
    ");
    $stmt->execute([$shop_id]);
    
    $notifications = [];
    $unread_count = 0;
    
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $notifications[] = [
            'id' => intval($row['id']),
            'order_id' => $row['order_id'] ? intval($row['order_id']) : null,
            'product_id' => isset($row['product_id']) && $row['product_id'] ? intval($row['product_id']) : null,
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
