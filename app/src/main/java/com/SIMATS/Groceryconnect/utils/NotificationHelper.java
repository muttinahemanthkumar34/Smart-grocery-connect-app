package com.SIMATS.Groceryconnect.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.activities.MainActivity;

/**
 * Helper class for creating and displaying notifications.
 * Handles notification channels for Android 8.0+ and builds styled notifications.
 */
public class NotificationHelper {
    
    private static final String CHANNEL_ID_ORDERS = "grocery_orders";
    private static final String CHANNEL_NAME_ORDERS = "Order Updates";
    private static final String CHANNEL_DESC_ORDERS = "Notifications about order status changes";
    
    private static final String CHANNEL_ID_GENERAL = "grocery_general";
    private static final String CHANNEL_NAME_GENERAL = "General Notifications";
    private static final String CHANNEL_DESC_GENERAL = "General app notifications";
    
    private Context context;
    private NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }
    
    /**
     * Create notification channels (required for Android 8.0+)
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Orders channel - high importance for order updates
            NotificationChannel ordersChannel = new NotificationChannel(
                    CHANNEL_ID_ORDERS,
                    CHANNEL_NAME_ORDERS,
                    NotificationManager.IMPORTANCE_HIGH
            );
            ordersChannel.setDescription(CHANNEL_DESC_ORDERS);
            ordersChannel.enableVibration(true);
            ordersChannel.enableLights(true);
            
            // General channel - default importance
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    CHANNEL_NAME_GENERAL,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription(CHANNEL_DESC_GENERAL);
            
            notificationManager.createNotificationChannel(ordersChannel);
            notificationManager.createNotificationChannel(generalChannel);
        }
    }
    
    /**
     * Show an order-related notification
     */
    public void showOrderNotification(String title, String message, int orderId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("notification_type", "order");
        intent.putExtra("order_id", orderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                orderId,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        showNotification(title, message, CHANNEL_ID_ORDERS, orderId, pendingIntent);
    }
    
    /**
     * Show a general notification
     */
    public void showGeneralNotification(String title, String message, int notificationId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        showNotification(title, message, CHANNEL_ID_GENERAL, notificationId, pendingIntent);
    }
    
    /**
     * Build and display notification
     */
    private void showNotification(String title, String message, String channelId, 
                                   int notificationId, PendingIntent pendingIntent) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_ai_sparkle) // Use app icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        
        // Add vibration
        notificationBuilder.setVibrate(new long[]{0, 500, 200, 500});
        
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    /**
     * Cancel a specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
