<?php
header("Content-Type: application/json");
require_once("config/db.php");

/* Validate required input */
if (!isset($_GET['shop_id']) || $_GET['shop_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "shop_id is required"
    ]);
    exit;
}

$shop_id = $_GET['shop_id'];
$category = $_GET['category'] ?? null;

try {
    if ($category) {
        /* Fetch products by shop + category */
        $stmt = $conn->prepare(
            "SELECT 
                product_id,
                product_name,
                category,
                price,
                stock_quantity,
                product_image
             FROM products
             WHERE shop_id = ? AND category = ?"
        );
        $stmt->execute([$shop_id, $category]);
    } else {
        /* Fetch all products of the shop */
        $stmt = $conn->prepare(
            "SELECT 
                product_id,
                product_name,
                category,
                price,
                stock_quantity,
                product_image
             FROM products
             WHERE shop_id = ?"
        );
        $stmt->execute([$shop_id]);
    }

    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => true,
        "products" => $products
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch products"
    ]);
}
