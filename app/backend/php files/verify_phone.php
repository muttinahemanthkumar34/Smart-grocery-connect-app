<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Validate required fields
if (empty($_POST['phone']) || empty($_POST['role'])) {
    echo json_encode([
        "status" => false,
        "message" => "Phone number and role are required"
    ]);
    exit;
}

$phone = trim($_POST['phone']);
$role = strtoupper(trim($_POST['role']));

// Remove any non-digit characters from phone
$phone = preg_replace('/[^0-9]/', '', $phone);

// Validate role
if ($role !== 'USER' && $role !== 'ADMIN') {
    echo json_encode([
        "status" => false,
        "message" => "Invalid role specified"
    ]);
    exit;
}

try {
    // Check if phone exists in users table with the specified role
    $stmt = $conn->prepare(
        "SELECT user_id, full_name, email, phone FROM users WHERE phone = ? AND role = ?"
    );
    $stmt->execute([$phone, $role]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        echo json_encode([
            "status" => true,
            "message" => "Phone number verified",
            "user_id" => $user['user_id'],
            "full_name" => $user['full_name'],
            "email" => $user['email']
        ]);
    } else {
        echo json_encode([
            "status" => false,
            "message" => "Phone number not registered as " . $role
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Database error: " . $e->getMessage()
    ]);
}
