<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Validate required fields
if (
    empty($_POST['shop_id']) ||
    empty($_POST['product_name']) ||
    empty($_POST['category']) ||
    !isset($_POST['price']) ||
    !isset($_POST['stock_quantity'])
) {
    echo json_encode([
        "status" => false,
        "message" => "shop_id, product_name, category, price, and stock_quantity are required"
    ]);
    exit;
}

$shop_id = intval($_POST['shop_id']);
$product_name = trim($_POST['product_name']);
$category = trim($_POST['category']);
$price = floatval($_POST['price']);
$stock_quantity = intval($_POST['stock_quantity']);
$product_image = isset($_POST['product_image']) ? trim($_POST['product_image']) : null;

// Handle base64 image upload
if (!empty($product_image) && strpos($product_image, 'data:image') === 0) {
    // Extract base64 data
    $image_parts = explode(";base64,", $product_image);
    if (count($image_parts) == 2) {
        $image_type_aux = explode("image/", $image_parts[0]);
        $image_type = $image_type_aux[1] ?? 'png';
        $image_base64 = base64_decode($image_parts[1]);
        
        // Create uploads directory if not exists
        $upload_dir = "uploads/products/";
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        // Generate unique filename
        $filename = "product_" . time() . "_" . rand(1000, 9999) . "." . $image_type;
        $filepath = $upload_dir . $filename;
        
        // Save image
        file_put_contents($filepath, $image_base64);
        $product_image = $filepath;
    }
}

try {
    $stmt = $conn->prepare(
        "INSERT INTO products (shop_id, product_name, category, price, stock_quantity, product_image) 
         VALUES (?, ?, ?, ?, ?, ?)"
    );
    $stmt->execute([$shop_id, $product_name, $category, $price, $stock_quantity, $product_image]);
    
    $product_id = $conn->lastInsertId();

    echo json_encode([
        "status" => true,
        "message" => "Product added successfully",
        "product_id" => intval($product_id)
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to add product: " . $e->getMessage()
    ]);
}
