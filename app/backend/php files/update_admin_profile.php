<?php
// Prevent any output before JSON
error_reporting(0);
ini_set('display_errors', 0);

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

$response = array();

// Include database config
require_once("config/db.php");

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $admin_id = isset($_POST['admin_id']) ? intval($_POST['admin_id']) : 0;
    $name = isset($_POST['name']) ? trim($_POST['name']) : '';
    $phone = isset($_POST['phone']) ? trim($_POST['phone']) : '';
    $profile_image = isset($_POST['profile_image']) ? trim($_POST['profile_image']) : null;
    
    // Validation
    if ($admin_id <= 0) {
        $response['success'] = false;
        $response['message'] = "Invalid admin ID";
        echo json_encode($response);
        exit;
    }
    
    if (empty($name)) {
        $response['success'] = false;
        $response['message'] = "Name is required";
        echo json_encode($response);
        exit;
    }
    
    if (empty($phone)) {
        $response['success'] = false;
        $response['message'] = "Phone number is required";
        echo json_encode($response);
        exit;
    }
    
    // Handle profile image upload if provided
    $image_path = null;
    if (!empty($profile_image) && strpos($profile_image, 'data:image') === 0) {
        $image_parts = explode(";base64,", $profile_image);
        if (count($image_parts) == 2) {
            $image_type_aux = explode("image/", $image_parts[0]);
            $image_type = $image_type_aux[1] ?? 'png';
            $image_base64 = base64_decode($image_parts[1]);
            
            $upload_dir = "uploads/profiles/";
            if (!file_exists($upload_dir)) {
                mkdir($upload_dir, 0777, true);
            }
            
            $filename = "admin_" . $admin_id . "_" . time() . "." . $image_type;
            $filepath = $upload_dir . $filename;
            
            if (file_put_contents($filepath, $image_base64)) {
                $image_path = $filepath;
            }
        }
    }
    
    try {
        // Build update query based on whether image is provided
        if ($image_path !== null) {
            $userSql = "UPDATE users SET full_name = ?, phone = ?, profile_image = ? WHERE user_id = ?";
            $userStmt = $conn->prepare($userSql);
            $userStmt->execute([$name, $phone, $image_path, $admin_id]);
        } else {
            $userSql = "UPDATE users SET full_name = ?, phone = ? WHERE user_id = ?";
            $userStmt = $conn->prepare($userSql);
            $userStmt->execute([$name, $phone, $admin_id]);
        }
        
        // Get updated profile image
        $getStmt = $conn->prepare("SELECT profile_image FROM users WHERE user_id = ?");
        $getStmt->execute([$admin_id]);
        $userData = $getStmt->fetch(PDO::FETCH_ASSOC);
        
        $response['success'] = true;
        $response['message'] = "Profile updated successfully";
        $response['profile_image'] = $userData['profile_image'] ?? null;
        
    } catch (PDOException $e) {
        $response['success'] = false;
        $response['message'] = "Database error: " . $e->getMessage();
    }
} else {
    $response['success'] = false;
    $response['message'] = "Invalid request method. Use POST.";
}

echo json_encode($response);
?>
