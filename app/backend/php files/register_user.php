<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (
    empty($_POST['full_name']) ||
    empty($_POST['email']) ||
    empty($_POST['phone']) ||
    empty($_POST['password'])
) {
    echo json_encode([
        "status" => false,
        "message" => "All fields are required"
    ]);
    exit;
}

$full_name = $_POST['full_name'];
$email = $_POST['email'];
$phone = $_POST['phone'];
$password = $_POST['password']; // ❌ no hashing (as requested)
$profile_image = isset($_POST['profile_image']) ? trim($_POST['profile_image']) : null;

// Handle base64 profile image upload (optional)
$image_path = null;
if (!empty($profile_image) && strpos($profile_image, 'data:image') === 0) {
    // Extract base64 data
    $image_parts = explode(";base64,", $profile_image);
    if (count($image_parts) == 2) {
        $image_type_aux = explode("image/", $image_parts[0]);
        $image_type = $image_type_aux[1] ?? 'png';
        $image_base64 = base64_decode($image_parts[1]);
        
        // Create uploads directory if not exists
        $upload_dir = "uploads/profiles/";
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        // Generate unique filename
        $filename = "user_" . time() . "_" . rand(1000, 9999) . "." . $image_type;
        $filepath = $upload_dir . $filename;
        
        // Save image
        if (file_put_contents($filepath, $image_base64)) {
            $image_path = $filepath;
        }
    }
}

try {
    $check = $conn->prepare("SELECT user_id FROM users WHERE email = ?");
    $check->execute([$email]);

    if ($check->rowCount() > 0) {
        echo json_encode([
            "status" => false,
            "message" => "Email already registered"
        ]);
        exit;
    }

    $stmt = $conn->prepare(
        "INSERT INTO users (full_name, email, phone, password_hash, role, profile_image)
         VALUES (?, ?, ?, ?, 'USER', ?)"
    );

    $stmt->execute([$full_name, $email, $phone, $password, $image_path]);

    echo json_encode([
        "status" => true,
        "message" => "User registered successfully"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Registration failed"
    ]);
}
