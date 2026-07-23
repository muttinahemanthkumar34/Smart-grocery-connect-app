<?php
header("Content-Type: application/json");
require_once("config/db.php");

// Get admin_id from request
if (!isset($_GET['admin_id']) || $_GET['admin_id'] === '') {
    echo json_encode([
        "status" => false,
        "message" => "admin_id is required"
    ]);
    exit;
}

$admin_id = intval($_GET['admin_id']);

try {
    // Get shop info for this admin
    $shopStmt = $conn->prepare(
        "SELECT shop_id, shop_name, shop_address, city, pincode, shop_phone, 
         opening_time, closing_time, delivery_radius, is_online, delivery_available, shop_image, upi_id 
         FROM shops WHERE admin_id = ?"
    );
    $shopStmt->execute([$admin_id]);
    $shop = $shopStmt->fetch(PDO::FETCH_ASSOC);

    if (!$shop) {
        echo json_encode([
            "status" => false,
            "message" => "No shop found for this admin"
        ]);
        exit;
    }

    $shop_id = $shop['shop_id'];
    $today = date('Y-m-d');

    // Get today's orders count
    $todayStmt = $conn->prepare(
        "SELECT COUNT(*) as count FROM orders 
         WHERE shop_id = ? AND DATE(created_at) = ?"
    );
    $todayStmt->execute([$shop_id, $today]);
    $todayOrders = $todayStmt->fetch(PDO::FETCH_ASSOC)['count'];

    // Get pending orders count (PLACED + PACKING)
    $pendingStmt = $conn->prepare(
        "SELECT COUNT(*) as count FROM orders 
         WHERE shop_id = ? AND order_status IN ('PLACED', 'PACKING')"
    );
    $pendingStmt->execute([$shop_id]);
    $pendingOrders = $pendingStmt->fetch(PDO::FETCH_ASSOC)['count'];

    // Get completed orders count (DELIVERED + READY)
    $completedStmt = $conn->prepare(
        "SELECT COUNT(*) as count FROM orders 
         WHERE shop_id = ? AND order_status IN ('DELIVERED', 'READY')"
    );
    $completedStmt->execute([$shop_id]);
    $completedOrders = $completedStmt->fetch(PDO::FETCH_ASSOC)['count'];

    // Get cancelled orders count
    $cancelledStmt = $conn->prepare(
        "SELECT COUNT(*) as count FROM orders 
         WHERE shop_id = ? AND order_status = 'CANCELLED'"
    );
    $cancelledStmt->execute([$shop_id]);
    $cancelledOrders = $cancelledStmt->fetch(PDO::FETCH_ASSOC)['count'];

    // Get today's total earnings from delivered orders (based on delivery date)
    $todayEarningsStmt = $conn->prepare(
        "SELECT COALESCE(SUM(total_amount), 0) as total FROM orders 
         WHERE shop_id = ? AND DATE(delivered_at) = ? AND order_status = 'DELIVERED'"
    );
    $todayEarningsStmt->execute([$shop_id, $today]);
    $todayEarnings = $todayEarningsStmt->fetch(PDO::FETCH_ASSOC)['total'];

    echo json_encode([
        "status" => true,
        "shop_id" => intval($shop_id),
        "shop_name" => $shop['shop_name'],
        "shop_address" => $shop['shop_address'] ?? "",
        "city" => $shop['city'] ?? "",
        "pincode" => $shop['pincode'] ?? "",
        "shop_phone" => $shop['shop_phone'] ?? "",
        "opening_time" => $shop['opening_time'] ?? "",
        "closing_time" => $shop['closing_time'] ?? "",
        "delivery_radius" => $shop['delivery_radius'] ?? "",
        "shop_image" => $shop['shop_image'] ?? "",
        "upi_id" => $shop['upi_id'] ?? "",
        "is_online" => intval($shop['is_online']),
        "delivery_available" => intval($shop['delivery_available']),
        "today_orders" => intval($todayOrders),
        "pending_orders" => intval($pendingOrders),
        "completed_orders" => intval($completedOrders),
        "cancelled_orders" => intval($cancelledOrders),
        "today_earnings" => floatval($todayEarnings)
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch dashboard data: " . $e->getMessage()
    ]);
}
