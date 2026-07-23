<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Validate required fields
if (empty($_POST['product_id'])) {
    echo json_encode([
        "status" => false,
        "message" => "product_id is required"
    ]);
    exit;
}

$product_id = intval($_POST['product_id']);
$product_name = isset($_POST['product_name']) ? trim($_POST['product_name']) : null;
$category = isset($_POST['category']) ? trim($_POST['category']) : null;
$price = isset($_POST['price']) ? floatval($_POST['price']) : null;
$stock_quantity = isset($_POST['stock_quantity']) ? intval($_POST['stock_quantity']) : null;
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
    // Build dynamic update query
    $updates = [];
    $params = [];

    if ($product_name !== null) {
        $updates[] = "product_name = ?";
        $params[] = $product_name;
    }
    if ($category !== null) {
        $updates[] = "category = ?";
        $params[] = $category;
    }
    if ($price !== null) {
        $updates[] = "price = ?";
        $params[] = $price;
    }
    if ($stock_quantity !== null) {
        $updates[] = "stock_quantity = ?";
        $params[] = $stock_quantity;
    }
    if ($product_image !== null && !empty($product_image)) {
        $updates[] = "product_image = ?";
        $params[] = $product_image;
    }

    if (empty($updates)) {
        echo json_encode([
            "status" => false,
            "message" => "No fields to update"
        ]);
        exit;
    }

    $params[] = $product_id;
    $sql = "UPDATE products SET " . implode(", ", $updates) . " WHERE product_id = ?";
    
    $stmt = $conn->prepare($sql);
    $stmt->execute($params);

    echo json_encode([
        "status" => true,
        "message" => "Product updated successfully"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to update product: " . $e->getMessage()
    ]);
}
