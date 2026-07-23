package com.SIMATS.Groceryconnect.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Model class for user notifications.
 */
public class Notification {
    private int id;
    private Integer orderId;
    private Integer productId;
    private String title;
    private String message;
    private boolean isRead;
    private String createdAt;
    private String orderStatus;

    public Notification() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    /**
     * Get relative time string (e.g., "2 hours ago", "Just now")
     */
    public String getTimeAgo() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(createdAt);
            if (date == null) return createdAt;
            
            long diffInMillis = System.currentTimeMillis() - date.getTime();
            
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            
            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else if (hours < 24) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (days < 7) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            return createdAt;
        }
    }

    /**
     * Get formatted date for display
     */
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return date != null ? outputFormat.format(date) : createdAt;
        } catch (ParseException e) {
            return createdAt;
        }
    }
}
