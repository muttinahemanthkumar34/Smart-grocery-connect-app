<?php
// Suppress warnings for clean JSON output
error_reporting(0);
ini_set('display_errors', 0);

header("Content-Type: application/json");
require_once("config/db.php");

// Set timezone to match your server
date_default_timezone_set('Asia/Kolkata');

// Validate required fields
if (empty($_POST['email']) || empty($_POST['otp'])) {
    echo json_encode([
        "status" => false,
        "message" => "Email and OTP are required"
    ]);
    exit;
}

$email = trim($_POST['email']);
$otp = trim($_POST['otp']);

try {
    // Get user by email
    $userStmt = $conn->prepare("SELECT user_id FROM users WHERE email = ?");
    $userStmt->execute([$email]);
    $user = $userStmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        echo json_encode([
            "status" => false,
            "message" => "User not found"
        ]);
        exit;
    }

    // First, let's check what OTPs exist for this user (for debugging)
    $debugStmt = $conn->prepare(
        "SELECT otp, expiry, used, created_at FROM password_reset_otp 
         WHERE user_id = ? ORDER BY created_at DESC LIMIT 1"
    );
    $debugStmt->execute([$user['user_id']]);
    $debugRecord = $debugStmt->fetch(PDO::FETCH_ASSOC);

    // Verify OTP - compare directly without NOW() function
    $currentTime = date('Y-m-d H:i:s');
    $otpStmt = $conn->prepare(
        "SELECT * FROM password_reset_otp 
         WHERE user_id = ? AND otp = ? AND used = 0 AND expiry > ?
         ORDER BY created_at DESC LIMIT 1"
    );
    $otpStmt->execute([$user['user_id'], $otp, $currentTime]);
    $otpRecord = $otpStmt->fetch(PDO::FETCH_ASSOC);

    if ($otpRecord) {
        // Mark OTP as used
        $updateStmt = $conn->prepare("UPDATE password_reset_otp SET used = 1 WHERE id = ?");
        $updateStmt->execute([$otpRecord['id']]);

        echo json_encode([
            "status" => true,
            "message" => "OTP verified successfully",
            "user_id" => $user['user_id']
        ]);
    } else {
        // Return debug info to help diagnose
        $debugInfo = "";
        if ($debugRecord) {
            $debugInfo = " (DB OTP: " . $debugRecord['otp'] . 
                        ", Entered: " . $otp . 
                        ", Expiry: " . $debugRecord['expiry'] . 
                        ", Now: " . $currentTime . 
                        ", Used: " . $debugRecord['used'] . ")";
        }
        
        echo json_encode([
            "status" => false,
            "message" => "Invalid or expired OTP" . $debugInfo
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Database error: " . $e->getMessage()
    ]);
}
