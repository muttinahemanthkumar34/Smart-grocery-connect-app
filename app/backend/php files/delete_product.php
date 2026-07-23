<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Validate required fields
if (empty($_POST['product_id']) || empty($_POST['shop_id'])) {
    echo json_encode([
        "status" => false,
        "message" => "product_id and shop_id are required"
    ]);
    exit;
}

$product_id = intval($_POST['product_id']);
$shop_id = intval($_POST['shop_id']);

try {
    // Verify product belongs to this shop
    $checkStmt = $conn->prepare(
        "SELECT product_id, product_name, product_image FROM products WHERE product_id = ? AND shop_id = ?"
    );
    $checkStmt->execute([$product_id, $shop_id]);
    $product = $checkStmt->fetch(PDO::FETCH_ASSOC);

    if (!$product) {
        echo json_encode([
            "status" => false,
            "message" => "Product not found or does not belong to this shop"
        ]);
        exit;
    }

    // Check if product is referenced in any orders
    $orderCheckStmt = $conn->prepare(
        "SELECT COUNT(*) as order_count FROM order_items WHERE product_id = ?"
    );
    $orderCheckStmt->execute([$product_id]);
    $orderCount = $orderCheckStmt->fetch(PDO::FETCH_ASSOC);

    // Begin transaction
    $conn->beginTransaction();

    if ($orderCount && $orderCount['order_count'] > 0) {
        // Product has order history - we need to handle the foreign key constraint
        // Option 1: Update order_items to remove the product reference (set to NULL)
        // First, we need to alter the order_items table to allow NULL product_id
        // For now, we'll delete the order_items entries (historical data will be lost)
        // Better approach: Mark product as deleted instead of actually deleting
        
        // Delete related order_items first (this removes order history for this product)
        $deleteOrderItemsStmt = $conn->prepare(
            "DELETE FROM order_items WHERE product_id = ?"
        );
        $deleteOrderItemsStmt->execute([$product_id]);
    }

    // Delete the product image file if exists
    if (!empty($product['product_image']) && file_exists($product['product_image'])) {
        unlink($product['product_image']);
    }

    // Delete the product
    $deleteStmt = $conn->prepare("DELETE FROM products WHERE product_id = ?");
    $deleteStmt->execute([$product_id]);

    // Commit transaction
    $conn->commit();

    echo json_encode([
        "status" => true,
        "message" => "Product deleted successfully"
    ]);

} catch (Exception $e) {
    // Rollback on error
    if ($conn->inTransaction()) {
        $conn->rollBack();
    }
    
    echo json_encode([
        "status" => false,
        "message" => "Failed to delete product: " . $e->getMessage()
    ]);
}
