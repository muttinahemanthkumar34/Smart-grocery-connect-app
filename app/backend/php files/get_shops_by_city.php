<?php
header("Content-Type: application/json");
require_once("config/db.php");

/* Validate input */
if (!isset($_GET['city']) || $_GET['city'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "City is required"
    ]);
    exit;
}

$city = strtolower(trim($_GET['city']));

try {
    // First, get all shops
    $stmt = $conn->prepare(
        "SELECT 
            shop_id,
            shop_name,
            category,
            shop_address,
            city,
            pincode,
            shop_phone,
            delivery_available,
            is_online,
            opening_time,
            closing_time,
            shop_image
         FROM shops
         WHERE LOWER(city) = ?"
    );

    $stmt->execute([$city]);
    $shops = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // For each shop, get their average rating
    foreach ($shops as &$shop) {
        $ratingStmt = $conn->prepare(
            "SELECT AVG(rating) as avg_rating, COUNT(*) as rating_count 
             FROM feedback 
             WHERE shop_id = ?"
        );
        $ratingStmt->execute([$shop['shop_id']]);
        $ratingData = $ratingStmt->fetch(PDO::FETCH_ASSOC);
        
        $shop['avg_rating'] = $ratingData['avg_rating'] !== null ? round((float)$ratingData['avg_rating'], 1) : 0;
        $shop['rating_count'] = (int)$ratingData['rating_count'];
    }

    echo json_encode([
        "status" => true,
        "shops" => $shops
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch shops: " . $e->getMessage()
    ]);
}
