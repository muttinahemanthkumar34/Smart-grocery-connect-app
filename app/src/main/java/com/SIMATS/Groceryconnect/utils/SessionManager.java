package com.SIMATS.Groceryconnect.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Session Manager for storing and retrieving user login session data.
 */
public class SessionManager {
    
    private static final String PREF_NAME = "GroceryConnectSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_PROFILE_IMAGE = "profileImage";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * Save user session after successful login
     */
    public void createLoginSession(int userId, String name, String email, String role, String phone, String profileImage) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_PROFILE_IMAGE, profileImage);
        editor.apply();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Get stored user ID
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Get stored user name
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }
    
    /**
     * Get stored user email
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * Get stored user role
     */
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "");
    }
    
    /**
     * Get stored user phone
     */
    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }
    
    /**
     * Update user name and phone in session
     */
    public void updateUserData(String name, String phone) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }
    
    /**
     * Update user name, phone and profile image in session
     */
    public void updateUserData(String name, String phone, String profileImage) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        if (profileImage != null) {
            editor.putString(KEY_PROFILE_IMAGE, profileImage);
        }
        editor.apply();
    }
    
    /**
     * Get profile image URL
     */
    public String getProfileImage() {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE, null);
    }
    
    /**
     * Set profile image URL
     */
    public void setProfileImage(String imageUrl) {
        editor.putString(KEY_PROFILE_IMAGE, imageUrl);
        editor.apply();
    }
    
    /**
     * Clear session data on logout
     */
    public void logout() {
        editor.clear();
        editor.apply();
        
        // Clear AI chat history for this user
        AiChatManager.getInstance().clearHistory();
    }
    
    // Notification preferences
    private static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    
    /**
     * Set notification preference
     */
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }
    
    /**
     * Get notification preference (default: true)
     */
    public boolean isNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }
    
    // FCM Token Storage
    private static final String KEY_FCM_TOKEN = "fcmToken";
    private static final String KEY_SHOP_ID = "shopId";
    
    /**
     * Save FCM token
     */
    public void setFcmToken(String token) {
        editor.putString(KEY_FCM_TOKEN, token);
        editor.apply();
    }
    
    /**
     * Get FCM token
     */
    public String getFcmToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }
    
    /**
     * Check if user is an admin
     */
    public boolean isAdminUser() {
        String role = getUserRole();
        return "admin".equalsIgnoreCase(role) || "shop".equalsIgnoreCase(role);
    }
    
    /**
     * Get user ID as string for FCM
     */
    public String getUserIdAsString() {
        int userId = getUserId();
        return userId > 0 ? String.valueOf(userId) : null;
    }
    
    /**
     * Set shop ID (for admin users)
     */
    public void setShopId(int shopId) {
        editor.putInt(KEY_SHOP_ID, shopId);
        editor.apply();
    }
    
    /**
     * Get shop ID (for admin users)
     */
    public String getShopId() {
        int shopId = sharedPreferences.getInt(KEY_SHOP_ID, -1);
        return shopId > 0 ? String.valueOf(shopId) : null;
    }
}
