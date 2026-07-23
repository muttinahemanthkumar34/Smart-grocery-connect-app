<?php
/**
 * Get all saved items for a user with product and shop details
 * Parameters: user_id
 */
error_reporting(0);
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

// Get user_id from GET or POST
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : (isset($_POST['user_id']) ? intval($_POST['user_id']) : 0);

// Validate input
if ($user_id <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid user_id'
    ]);
    exit;
}

try {
    // Create table if not exists
    $createTable = "CREATE TABLE IF NOT EXISTS saved_items (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        product_id INT NOT NULL,
        shop_id INT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE KEY unique_save (user_id, product_id)
    )";
    $conn->exec($createTable);
    
    // Fetch saved items with product and shop details
    $query = "SELECT 
                si.id as saved_id,
                si.product_id,
                si.shop_id,
                si.created_at as saved_at,
                p.product_name,
                p.price,
                p.stock_quantity,
                p.category,
                p.product_image,
                s.shop_name
              FROM saved_items si
              JOIN products p ON si.product_id = p.product_id
              JOIN shops s ON si.shop_id = s.shop_id
              WHERE si.user_id = :user_id
              ORDER BY si.created_at DESC";
    
    $stmt = $conn->prepare($query);
    $stmt->execute([':user_id' => $user_id]);
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    $savedItems = [];
    foreach ($results as $row) {
        $savedItems[] = [
            'saved_id' => intval($row['saved_id']),
            'product_id' => intval($row['product_id']),
            'shop_id' => intval($row['shop_id']),
            'product_name' => $row['product_name'],
            'price' => floatval($row['price']),
            'stock_quantity' => intval($row['stock_quantity']),
            'category' => $row['category'],
            'image_url' => $row['product_image'],
            'shop_name' => $row['shop_name'],
            'saved_at' => $row['saved_at']
        ];
    }
    
    echo json_encode([
        'success' => true,
        'saved_items' => $savedItems,
        'count' => count($savedItems)
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
