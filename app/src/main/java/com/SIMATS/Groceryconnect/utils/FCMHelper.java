package com.SIMATS.Groceryconnect.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for Firebase Cloud Messaging operations.
 * Handles token retrieval, registration, and permission requests.
 */
public class FCMHelper {
    
    private static final String TAG = "FCMHelper";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    
    /**
     * Request notification permission (required for Android 13+)
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }
    
    /**
     * Get FCM token and send to server.
     * Call this after successful login.
     */
    public static void registerToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    
                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                    
                    // Save and send to server
                    sendTokenToServer(context, token);
                });
    }
    
    /**
     * Send FCM token to backend server
     */
    private static void sendTokenToServer(Context context, String token) {
        SessionManager sessionManager = new SessionManager(context);
        
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping token registration");
            return;
        }
        
        // Save token locally
        sessionManager.setFcmToken(token);
        
        String userId = sessionManager.getUserIdAsString();
        String shopId = sessionManager.getShopId();
        String userType = sessionManager.isAdminUser() ? "admin" : "user";
        
        Log.d(TAG, "Registering FCM token for " + userType + " (userId: " + userId + ", shopId: " + shopId + ")");
        
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.BASE_URL + "save_fcm_token.php",
                response -> Log.d(TAG, "Token registered successfully: " + response),
                error -> Log.e(TAG, "Error registering token: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"))
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fcm_token", token);
                params.put("user_type", userType);
                if (userId != null) params.put("user_id", userId);
                if (shopId != null) params.put("shop_id", shopId);
                params.put("device_type", "android");
                return params;
            }
        };
        
        requestQueue.add(request);
    }
    
    /**
     * Unregister FCM token from server (e.g., on logout)
     */
    public static void unregisterToken(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getFcmToken();
        
        if (token == null) {
            return;
        }
        
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.BASE_URL + "delete_fcm_token.php",
                response -> {
                    Log.d(TAG, "Token unregistered: " + response);
                    sessionManager.setFcmToken(null);
                },
                error -> Log.e(TAG, "Error unregistering token")
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fcm_token", token);
                return params;
            }
        };
        
        requestQueue.add(request);
    }
}
