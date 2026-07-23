<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once("config/db.php");

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $shop_id = isset($_POST['shop_id']) ? intval($_POST['shop_id']) : 0;
    $shop_name = isset($_POST['shop_name']) ? trim($_POST['shop_name']) : '';
    $shop_address = isset($_POST['shop_address']) ? trim($_POST['shop_address']) : '';
    $city = isset($_POST['city']) ? trim($_POST['city']) : '';
    $pincode = isset($_POST['pincode']) ? trim($_POST['pincode']) : '';
    $shop_phone = isset($_POST['shop_phone']) ? trim($_POST['shop_phone']) : '';
    $opening_time = isset($_POST['opening_time']) ? trim($_POST['opening_time']) : '';
    $closing_time = isset($_POST['closing_time']) ? trim($_POST['closing_time']) : '';
    $delivery_radius = isset($_POST['delivery_radius']) ? floatval($_POST['delivery_radius']) : 0;
    $shop_image = isset($_POST['shop_image']) ? trim($_POST['shop_image']) : null;
    $upi_id = isset($_POST['upi_id']) ? trim($_POST['upi_id']) : null;
    
    // Validation
    if ($shop_id <= 0) {
        $response['success'] = false;
        $response['message'] = "Invalid shop ID";
        echo json_encode($response);
        exit;
    }
    
    if (empty($shop_name)) {
        $response['success'] = false;
        $response['message'] = "Shop name is required";
        echo json_encode($response);
        exit;
    }
    
    // Ensure upi_id column exists
    try {
        $conn->exec("ALTER TABLE shops ADD COLUMN upi_id VARCHAR(100) NULL");
    } catch (Exception $e) {
        // Column may already exist
    }
    
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
            $filename = "shop_" . $shop_id . "_" . time() . "." . $image_type;
            $filepath = $upload_dir . $filename;
            
            // Save image
            if (file_put_contents($filepath, $image_base64)) {
                $image_path = $filepath;
            }
        }
    }
    
    try {
        // Build update query dynamically
        $updateFields = [];
        $params = [];
        
        $updateFields[] = "shop_name = ?";
        $params[] = $shop_name;
        
        if (!empty($shop_address)) {
            $updateFields[] = "shop_address = ?";
            $params[] = $shop_address;
        }
        
        if (!empty($city)) {
            $updateFields[] = "city = ?";
            $params[] = $city;
        }
        
        if (!empty($pincode)) {
            $updateFields[] = "pincode = ?";
            $params[] = $pincode;
        }
        
        if (!empty($shop_phone)) {
            $updateFields[] = "shop_phone = ?";
            $params[] = $shop_phone;
        }
        
        if (!empty($opening_time)) {
            $updateFields[] = "opening_time = ?";
            $params[] = $opening_time;
        }
        
        if (!empty($closing_time)) {
            $updateFields[] = "closing_time = ?";
            $params[] = $closing_time;
        }
        
        if ($delivery_radius > 0) {
            $updateFields[] = "delivery_radius = ?";
            $params[] = $delivery_radius;
        }
        
        if ($image_path !== null) {
            $updateFields[] = "shop_image = ?";
            $params[] = $image_path;
        }
        
        if ($upi_id !== null) {
            $updateFields[] = "upi_id = ?";
            $params[] = $upi_id;
        }
        
        $params[] = $shop_id;
        
        $sql = "UPDATE shops SET " . implode(", ", $updateFields) . " WHERE shop_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute($params);
        
        $response['success'] = true;
        $response['message'] = "Shop updated successfully";
        if ($image_path !== null) {
            $response['shop_image'] = $image_path;
        }
        
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
