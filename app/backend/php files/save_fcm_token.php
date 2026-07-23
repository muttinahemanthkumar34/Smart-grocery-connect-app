<?php
/**
 * Save FCM Token for Push Notifications
 * 
 * This script saves or updates FCM tokens for users and admins.
 * Create the fcm_tokens table first:
 * 
 * CREATE TABLE fcm_tokens (
 *     id INT AUTO_INCREMENT PRIMARY KEY,
 *     user_id INT NULL,
 *     shop_id INT NULL,
 *     user_type ENUM('user', 'admin') NOT NULL,
 *     fcm_token VARCHAR(255) NOT NULL,
 *     device_type VARCHAR(50) DEFAULT 'android',
 *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *     UNIQUE KEY unique_token (fcm_token)
 * );
 */

header('Content-Type: application/json');

// Use same db config as other files
require_once("db.php");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => false, 'message' => 'Invalid request method']);
    exit;
}

$fcm_token = isset($_POST['fcm_token']) ? trim($_POST['fcm_token']) : '';
$user_type = isset($_POST['user_type']) ? trim($_POST['user_type']) : 'user';
$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : null;
$shop_id = isset($_POST['shop_id']) ? intval($_POST['shop_id']) : null;
$device_type = isset($_POST['device_type']) ? trim($_POST['device_type']) : 'android';

// Log incoming request for debugging
error_log("FCM Token Request - token: " . substr($fcm_token, 0, 20) . "..., user_type: $user_type, user_id: $user_id, shop_id: $shop_id");

if (empty($fcm_token)) {
    echo json_encode(['status' => false, 'message' => 'FCM token is required']);
    exit;
}

// Validate that user tokens have a valid user_id
if ($user_type === 'user' && ($user_id === null || $user_id === 0)) {
    error_log("WARNING: Attempting to save user token without valid user_id");
}

try {
    // Check if token already exists (using PDO)
    $checkStmt = $conn->prepare("SELECT id FROM fcm_tokens WHERE fcm_token = ?");
    $checkStmt->execute([$fcm_token]);
    $existing = $checkStmt->fetch(PDO::FETCH_ASSOC);
    
    if ($existing) {
        // Update existing token
        $stmt = $conn->prepare("UPDATE fcm_tokens SET user_id = ?, shop_id = ?, user_type = ?, device_type = ? WHERE fcm_token = ?");
        $stmt->execute([$user_id, $shop_id, $user_type, $device_type, $fcm_token]);
        error_log("FCM Token updated successfully");
    } else {
        // Insert new token
        $stmt = $conn->prepare("INSERT INTO fcm_tokens (user_id, shop_id, user_type, fcm_token, device_type) VALUES (?, ?, ?, ?, ?)");
        $stmt->execute([$user_id, $shop_id, $user_type, $fcm_token, $device_type]);
        error_log("FCM Token inserted successfully");
    }
    
    echo json_encode(['status' => true, 'message' => 'Token saved successfully']);
    
} catch (Exception $e) {
    error_log("FCM Token Error: " . $e->getMessage());
    echo json_encode(['status' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
