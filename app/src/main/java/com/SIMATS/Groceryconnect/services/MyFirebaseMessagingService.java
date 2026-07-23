package com.SIMATS.Groceryconnect.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.NotificationHelper;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications and token refresh events.
 * Works even when app is in background or closed.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    
    /**
     * Called when a new FCM token is generated.
     * This happens on first app start and when token is refreshed.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        
        // Save token locally and send to server
        sendTokenToServer(token);
    }
    
    /**
     * Called when a message is received.
     * This is called for both data messages and notification messages when app is in foreground.
     * For background, notification messages are handled by system, data messages come here.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());
        
        String title = "GroceryConnect";
        String message = "";
        String type = "general";
        int orderId = 0;
        
        // Check for data payload (works in background)
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            title = data.getOrDefault("title", title);
            message = data.getOrDefault("message", "");
            type = data.getOrDefault("type", "general");
            try {
                orderId = Integer.parseInt(data.getOrDefault("order_id", "0"));
            } catch (NumberFormatException e) {
                orderId = 0;
            }
        }
        
        // Check for notification payload (only works in foreground)
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                message = remoteMessage.getNotification().getBody();
            }
        }
        
        // Show notification
        if (message != null && !message.isEmpty()) {
            NotificationHelper notificationHelper = new NotificationHelper(this);
            
            if ("order".equals(type) && orderId > 0) {
                notificationHelper.showOrderNotification(title, message, orderId);
            } else {
                // Use current time as notification ID to avoid overwriting
                int notificationId = (int) System.currentTimeMillis();
                notificationHelper.showGeneralNotification(title, message, notificationId);
            }
        }
    }
    
    /**
     * Send FCM token to backend server
     */
    private void sendTokenToServer(String token) {
        SessionManager sessionManager = new SessionManager(this);
        
        // Only send if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping token upload");
            return;
        }
        
        // Save token locally
        sessionManager.setFcmToken(token);
        
        String userId = sessionManager.getUserIdAsString();
        String shopId = sessionManager.getShopId();
        String userType = sessionManager.isAdminUser() ? "admin" : "user";
        
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.BASE_URL + "save_fcm_token.php",
                response -> Log.d(TAG, "Token saved to server: " + response),
                error -> Log.e(TAG, "Error saving token: " + error.getMessage())
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
}
