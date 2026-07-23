package com.SIMATS.Groceryconnect.network;

/**
 * Centralized API configuration for the GroceryConnect app.
 * Change the BASE_URL when deploying to different servers.
 */
public class ApiConfig {
    
    // Base URL for the PHP backend
    // Use 10.0.2.2 for Android emulator (localhost on host machine)
    // Use actual IP address for physical devices on same network
    public static final String BASE_URL = "http://10.135.251.20:8001/";
    
    // Image Base URL for product images (database stores relative path like 'uploads/products/...')
    public static final String IMAGE_BASE_URL = BASE_URL;
    
    // Auth Endpoints
    public static final String LOGIN_URL = BASE_URL + "login.php";
    public static final String REGISTER_USER_URL = BASE_URL + "register_user.php";
    public static final String REGISTER_ADMIN_URL = BASE_URL + "register_admin.php";
    
    // User Endpoints
    public static final String GET_USER_DEFAULT_ADDRESS_URL = BASE_URL + "get_user_default_address.php";
    public static final String UPDATE_USER_URL = BASE_URL + "update_user.php";
    
    // Address Endpoints
    public static final String GET_USER_ADDRESSES_URL = BASE_URL + "get_user_addresses.php";
    public static final String ADD_ADDRESS_URL = BASE_URL + "add_address.php";
    public static final String UPDATE_ADDRESS_URL = BASE_URL + "update_address.php";
    public static final String DELETE_ADDRESS_URL = BASE_URL + "delete_address.php";
    
    // Shop Endpoints
    public static final String GET_SHOPS_BY_CITY_URL = BASE_URL + "get_shops_by_city.php";
    
    // Product Endpoints
    public static final String GET_PRODUCTS_BY_SHOP_URL = BASE_URL + "get_products_by_shop.php";
    
    // Order Endpoints
    public static final String PLACE_ORDER_URL = BASE_URL + "place_order.php";
    public static final String GET_ORDER_DETAILS_URL = BASE_URL + "get_order_details.php";
    public static final String GET_USER_ORDERS_URL = BASE_URL + "get_user_orders.php";
    public static final String CANCEL_ORDER_URL = BASE_URL + "cancel_order.php";
    
    // Feedback Endpoints
    public static final String ADD_FEEDBACK_URL = BASE_URL + "add_feedback.php";
    
    // Admin Dashboard Endpoints
    public static final String GET_ADMIN_DASHBOARD_URL = BASE_URL + "get_admin_dashboard.php";
    public static final String UPDATE_SHOP_STATUS_URL = BASE_URL + "update_shop_status.php";
    
    // Admin Profile Endpoints
    public static final String UPDATE_ADMIN_PROFILE_URL = BASE_URL + "update_admin_profile.php";
    public static final String GET_SHOP_FEEDBACKS_URL = BASE_URL + "get_shop_feedback.php";
    
    // Admin Order Management Endpoints
    public static final String GET_ADMIN_ORDERS_URL = BASE_URL + "get_admin_orders.php";
    public static final String UPDATE_ORDER_STATUS_URL = BASE_URL + "update_order_status.php";
    
    // Admin Stock Management Endpoints
    public static final String GET_SHOP_PRODUCTS_URL = BASE_URL + "get_shop_products.php";
    public static final String ADD_PRODUCT_URL = BASE_URL + "add_product.php";
    public static final String UPDATE_PRODUCT_URL = BASE_URL + "update_product.php";
    public static final String DELETE_PRODUCT_URL = BASE_URL + "delete_product.php";
    
    // Shop Management Endpoints
    public static final String UPDATE_SHOP_URL = BASE_URL + "update_shop.php";
    
    // Forgot Password Endpoints (Email-based OTP)
    public static final String SEND_EMAIL_OTP_URL = BASE_URL + "send_email_otp.php";
    public static final String VERIFY_EMAIL_OTP_URL = BASE_URL + "verify_email_otp.php";
    public static final String RESET_PASSWORD_URL = BASE_URL + "reset_password.php";
    
    // Notification Endpoints (User)
    public static final String GET_NOTIFICATIONS_URL = BASE_URL + "get_notifications.php";
    public static final String MARK_NOTIFICATION_READ_URL = BASE_URL + "mark_notification_read.php";
    public static final String UPDATE_NOTIFICATION_SETTING_URL = BASE_URL + "update_notification_setting.php";
    public static final String GET_NOTIFICATION_SETTING_URL = BASE_URL + "get_notification_setting.php";
    public static final String CLEAR_NOTIFICATIONS_URL = BASE_URL + "clear_notifications.php";
    
    // Notification Endpoints (Admin)
    public static final String GET_ADMIN_NOTIFICATIONS_URL = BASE_URL + "get_admin_notifications.php";
    public static final String MARK_ADMIN_NOTIFICATION_READ_URL = BASE_URL + "mark_admin_notification_read.php";
    public static final String CLEAR_ADMIN_NOTIFICATIONS_URL = BASE_URL + "clear_admin_notifications.php";
    public static final String GET_ADMIN_NOTIFICATION_SETTING_URL = BASE_URL + "get_admin_notification_setting.php";
    public static final String UPDATE_ADMIN_NOTIFICATION_SETTING_URL = BASE_URL + "update_admin_notification_setting.php";
    
    // Saved Items Endpoints
    public static final String SAVE_ITEM_URL = BASE_URL + "save_item.php";
    public static final String GET_SAVED_ITEMS_URL = BASE_URL + "get_saved_items.php";
    public static final String CHECK_SAVED_ITEMS_URL = BASE_URL + "check_saved_items.php";
    
    // Payment Endpoints
    public static final String GET_SHOP_UPI_URL = BASE_URL + "get_shop_upi.php";
    
    // AI Assistant Endpoint - pointing directly to the PHP backend assistant
    // (If you want to use the FastAPI Python server, uncomment the lines below and comment the one above)
    public static final String AI_ASSISTANT_URL = BASE_URL + "ai_assistant.php";
    
    // public static final String FASTAPI_BASE_URL = "http://10.0.2.2:8000/";
    // public static final String AI_ASSISTANT_URL = FASTAPI_BASE_URL + "api/ai-assistant";
}
