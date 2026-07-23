<?php
/**
 * Toggle save/unsave a product for a user
 * Parameters: user_id, product_id, shop_id
 */
error_reporting(0);
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

// Get POST data
$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
$product_id = isset($_POST['product_id']) ? intval($_POST['product_id']) : 0;
$shop_id = isset($_POST['shop_id']) ? intval($_POST['shop_id']) : 0;

// Validate input
if ($user_id <= 0 || $product_id <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid user_id or product_id'
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
    
    // Check if item is already saved
    $checkQuery = "SELECT id FROM saved_items WHERE user_id = :user_id AND product_id = :product_id";
    $checkStmt = $conn->prepare($checkQuery);
    $checkStmt->execute([':user_id' => $user_id, ':product_id' => $product_id]);
    $existing = $checkStmt->fetch();
    
    if ($existing) {
        // Item exists, remove it (unsave)
        $deleteQuery = "DELETE FROM saved_items WHERE user_id = :user_id AND product_id = :product_id";
        $deleteStmt = $conn->prepare($deleteQuery);
        $deleteStmt->execute([':user_id' => $user_id, ':product_id' => $product_id]);
        
        echo json_encode([
            'success' => true,
            'saved' => false,
            'message' => 'Item removed from saved items'
        ]);
    } else {
        // Item doesn't exist, add it (save)
        $insertQuery = "INSERT INTO saved_items (user_id, product_id, shop_id) VALUES (:user_id, :product_id, :shop_id)";
        $insertStmt = $conn->prepare($insertQuery);
        $insertStmt->execute([':user_id' => $user_id, ':product_id' => $product_id, ':shop_id' => $shop_id]);
        
        echo json_encode([
            'success' => true,
            'saved' => true,
            'message' => 'Item saved successfully'
        ]);
    }
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
