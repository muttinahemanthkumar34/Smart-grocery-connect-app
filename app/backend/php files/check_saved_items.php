<?php
/**
 * Check which products are saved by a user
 * Parameters: user_id, product_ids (comma-separated list)
 */
error_reporting(0);
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

// Get parameters
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : (isset($_POST['user_id']) ? intval($_POST['user_id']) : 0);
$product_ids = isset($_GET['product_ids']) ? $_GET['product_ids'] : (isset($_POST['product_ids']) ? $_POST['product_ids'] : '');

// Validate input
if ($user_id <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid user_id'
    ]);
    exit;
}

if (empty($product_ids)) {
    echo json_encode([
        'success' => true,
        'saved_product_ids' => []
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
    
    // Parse product IDs
    $productIdArray = array_map('intval', explode(',', $product_ids));
    $productIdArray = array_filter($productIdArray, function($id) { return $id > 0; });
    
    if (empty($productIdArray)) {
        echo json_encode([
            'success' => true,
            'saved_product_ids' => []
        ]);
        exit;
    }
    
    // Create placeholders for IN clause
    $placeholders = implode(',', array_fill(0, count($productIdArray), '?'));
    
    // Fetch saved product IDs
    $query = "SELECT product_id FROM saved_items WHERE user_id = ? AND product_id IN ($placeholders)";
    $stmt = $conn->prepare($query);
    
    // Bind parameters (user_id + all product_ids)
    $params = array_merge([$user_id], $productIdArray);
    $stmt->execute($params);
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    $savedProductIds = [];
    foreach ($results as $row) {
        $savedProductIds[] = intval($row['product_id']);
    }
    
    echo json_encode([
        'success' => true,
        'saved_product_ids' => $savedProductIds
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
