<?php
header("Content-Type: application/json");
require_once("config/db.php");

if (
    empty($_POST['email']) ||
    empty($_POST['password']) ||
    empty($_POST['role'])
) {
    echo json_encode([
        "status" => false,
        "message" => "Email, password and role are required"
    ]);
    exit;
}

$email = $_POST['email'];
$password = $_POST['password'];
$role = $_POST['role']; // USER or ADMIN

$stmt = $conn->prepare(
    "SELECT user_id, full_name, email, phone, role, password_hash, profile_image
     FROM users
     WHERE email = ? AND role = ?"
);
$stmt->execute([$email, $role]);
$user = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$user || $user['password_hash'] !== $password) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid credentials or role mismatch"
    ]);
    exit;
}

// Build response
$response = [
    "status" => true,
    "user_id" => $user['user_id'],
    "full_name" => $user['full_name'],
    "email" => $user['email'],
    "phone" => $user['phone'],
    "role" => $user['role'],
    "profile_image" => $user['profile_image'],
    "message" => "Login successful"
];

// For admin users, also get their shop_id
if (strtoupper($user['role']) === 'ADMIN') {
    $shopStmt = $conn->prepare("SELECT shop_id FROM shops WHERE admin_id = ?");
    $shopStmt->execute([$user['user_id']]);
    $shop = $shopStmt->fetch(PDO::FETCH_ASSOC);
    if ($shop) {
        $response['shop_id'] = $shop['shop_id'];
    }
}

echo json_encode($response);

