<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Required admin fields
if (
    empty($_POST['full_name']) ||
    empty($_POST['email']) ||
    empty($_POST['phone']) ||
    empty($_POST['password'])
) {
    echo json_encode([
        "status" => false,
        "message" => "Admin details are required"
    ]);
    exit;
}

// Required shop fields
$shopFields = ['shop_name', 'shop_address', 'city', 'pincode', 'shop_phone', 
               'category', 'opening_time', 'closing_time', 'delivery_radius'];

foreach ($shopFields as $field) {
    if (empty($_POST[$field])) {
        echo json_encode([
            "status" => false,
            "message" => "Missing field: $field"
        ]);
        exit;
    }
}

// Admin data
$full_name = $_POST['full_name'];
$email = $_POST['email'];
$phone = $_POST['phone'];
$password = $_POST['password'];
$admin_profile_image = isset($_POST['profile_image']) ? trim($_POST['profile_image']) : null;

// Handle admin profile image upload (optional)
$admin_image_path = null;
if (!empty($admin_profile_image) && strpos($admin_profile_image, 'data:image') === 0) {
    $image_parts = explode(";base64,", $admin_profile_image);
    if (count($image_parts) == 2) {
        $image_type_aux = explode("image/", $image_parts[0]);
        $image_type = $image_type_aux[1] ?? 'png';
        $image_base64 = base64_decode($image_parts[1]);
        
        $upload_dir = "uploads/profiles/";
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        $filename = "admin_" . time() . "_" . rand(1000, 9999) . "." . $image_type;
        $filepath = $upload_dir . $filename;
        
        if (file_put_contents($filepath, $image_base64)) {
            $admin_image_path = $filepath;
        }
    }
}

// Shop data
$shop_name = $_POST['shop_name'];
$shop_address = $_POST['shop_address'];
$city = $_POST['city'];
$pincode = $_POST['pincode'];
$shop_phone = $_POST['shop_phone'];
$category = strtoupper($_POST['category']); // Convert to match ENUM
$opening_time = $_POST['opening_time'];
$closing_time = $_POST['closing_time'];
$delivery_radius = $_POST['delivery_radius'];
$delivery_available = $_POST['delivery_available'] ?? 1;
$shop_image = isset($_POST['shop_image']) ? trim($_POST['shop_image']) : null;

// Handle base64 image upload
$image_path = null;
if (!empty($shop_image) && strpos($shop_image, 'data:image') === 0) {
    // Extract base64 data
    $image_parts = explode(";base64,", $shop_image);
    if (count($image_parts) == 2) {
        $image_type_aux = explode("image/", $image_parts[0]);
        $image_type = $image_type_aux[1] ?? 'png';
        $image_base64 = base64_decode($image_parts[1]);
        
        // Create uploads directory if not exists
        $upload_dir = "uploads/shops/";
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        // Generate unique filename
        $filename = "shop_" . time() . "_" . rand(1000, 9999) . "." . $image_type;
        $filepath = $upload_dir . $filename;
        
        // Save image
        if (file_put_contents($filepath, $image_base64)) {
            $image_path = $filepath;
        }
    }
}

// Map category to ENUM values
$categoryMap = [
    'GROCERY' => 'GROCERY',
    'VEGETABLES' => 'GROCERY',
    'FRUITS' => 'GROCERY',
    'GENERAL STORE' => 'GROCERY',
    'SUPERMARKET' => 'SUPERMART',
    'PHARMACY' => 'GROCERY'
];
$dbCategory = $categoryMap[$category] ?? 'GROCERY';

try {
    // Check if email already exists
    $check = $conn->prepare("SELECT user_id FROM users WHERE email = ?");
    $check->execute([$email]);

    if ($check->rowCount() > 0) {
        echo json_encode([
            "status" => false,
            "message" => "Email already registered"
        ]);
        exit;
    }

    // Start transaction
    $conn->beginTransaction();

    // Insert admin user with profile image
    $stmt = $conn->prepare(
        "INSERT INTO users (full_name, email, phone, password_hash, role, profile_image)
         VALUES (?, ?, ?, ?, 'ADMIN', ?)"
    );
    $stmt->execute([$full_name, $email, $phone, $password, $admin_image_path]);
    $admin_id = $conn->lastInsertId();

    // Insert shop (matching existing schema)
    $shopStmt = $conn->prepare(
        "INSERT INTO shops (admin_id, shop_name, category, shop_address, city, pincode,
         opening_time, closing_time, delivery_available, shop_phone, delivery_radius, shop_image)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    );
    $shopStmt->execute([
        $admin_id, $shop_name, $dbCategory, $shop_address, $city, $pincode,
        $opening_time, $closing_time, $delivery_available, $shop_phone, $delivery_radius, $image_path
    ]);

    // Commit transaction
    $conn->commit();

    echo json_encode([
        "status" => true,
        "admin_id" => $admin_id,
        "message" => "Admin and shop registered successfully"
    ]);

} catch (Exception $e) {
    $conn->rollBack();
    echo json_encode([
        "status" => false,
        "message" => "Registration failed: " . $e->getMessage()
    ]);
}
