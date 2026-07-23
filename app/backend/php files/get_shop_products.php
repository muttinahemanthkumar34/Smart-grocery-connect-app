<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Get shop_id from request
if (!isset($_GET['shop_id']) || $_GET['shop_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "shop_id is required"
    ]);
    exit;
}

$shop_id = intval($_GET['shop_id']);

try {
    // Get all products for this shop
    $stmt = $conn->prepare(
        "SELECT product_id, product_name, category, price, stock_quantity, product_image, created_at 
         FROM products WHERE shop_id = ? ORDER BY category, product_name"
    );
    $stmt->execute([$shop_id]);
    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Get distinct categories
    $catStmt = $conn->prepare(
        "SELECT DISTINCT category FROM products WHERE shop_id = ? ORDER BY category"
    );
    $catStmt->execute([$shop_id]);
    $categories = $catStmt->fetchAll(PDO::FETCH_COLUMN);

    // Format products with low stock indicator
    $formattedProducts = [];
    foreach ($products as $product) {
        $formattedProducts[] = [
            "product_id" => intval($product['product_id']),
            "product_name" => $product['product_name'],
            "category" => $product['category'],
            "price" => floatval($product['price']),
            "stock_quantity" => intval($product['stock_quantity']),
            "product_image" => $product['product_image'] ?? "",
            "is_low_stock" => intval($product['stock_quantity']) < 10,
            "created_at" => $product['created_at']
        ];
    }

    echo json_encode([
        "status" => true,
        "products" => $formattedProducts,
        "categories" => $categories,
        "total_products" => count($products)
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch products: " . $e->getMessage()
    ]);
}
