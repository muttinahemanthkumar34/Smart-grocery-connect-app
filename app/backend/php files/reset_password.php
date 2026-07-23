<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Validate required fields
if (empty($_POST['email']) || empty($_POST['new_password']) || empty($_POST['role'])) {
    echo json_encode([
        "status" => false,
        "message" => "Email, new password, and role are required"
    ]);
    exit;
}

$email = trim($_POST['email']);
$new_password = $_POST['new_password'];
$role = strtoupper(trim($_POST['role']));

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid email format"
    ]);
    exit;
}

// Validate password length
if (strlen($new_password) < 6) {
    echo json_encode([
        "status" => false,
        "message" => "Password must be at least 6 characters"
    ]);
    exit;
}

// Validate role
if ($role !== 'USER' && $role !== 'ADMIN') {
    echo json_encode([
        "status" => false,
        "message" => "Invalid role specified"
    ]);
    exit;
}

try {
    // Verify email exists
    $checkStmt = $conn->prepare(
        "SELECT user_id FROM users WHERE email = ? AND role = ?"
    );
    $checkStmt->execute([$email, $role]);
    $user = $checkStmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        echo json_encode([
            "status" => false,
            "message" => "Email not found"
        ]);
        exit;
    }

    // Update password (storing plain text as per existing app pattern)
    // Note: In production, use password_hash() for security
    $updateStmt = $conn->prepare(
        "UPDATE users SET password_hash = ? WHERE user_id = ?"
    );
    $updateStmt->execute([$new_password, $user['user_id']]);

    if ($updateStmt->rowCount() > 0) {
        // Clean up used OTPs
        $cleanupStmt = $conn->prepare("DELETE FROM password_reset_otp WHERE user_id = ?");
        $cleanupStmt->execute([$user['user_id']]);
        
        echo json_encode([
            "status" => true,
            "message" => "Password reset successfully"
        ]);
    } else {
        echo json_encode([
            "status" => false,
            "message" => "Failed to update password"
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Database error: " . $e->getMessage()
    ]);
}
