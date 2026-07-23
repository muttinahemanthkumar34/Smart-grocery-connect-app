package com.SIMATS.Groceryconnect.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class for user orders.
 */
public class Order {
    private int orderId;
    private int shopId;
    private String shopName;
    private String orderType;
    private double totalAmount;
    private String orderStatus;
    private String createdAt;
    private boolean feedbackSubmitted;

    // Status constants
    public static final String STATUS_PLACED = "PLACED";
    public static final String STATUS_PACKING = "PACKING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public Order() {}

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isFeedbackSubmitted() { return feedbackSubmitted; }
    public void setFeedbackSubmitted(boolean feedbackSubmitted) { this.feedbackSubmitted = feedbackSubmitted; }

    /**
     * Get formatted date string for display (e.g., "December 26, 2025")
     */
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return date != null ? outputFormat.format(date) : createdAt;
        } catch (ParseException e) {
            return createdAt;
        }
    }

    /**
     * Get formatted amount with Rupee symbol
     */
    public String getFormattedAmount() {
        return String.format(Locale.getDefault(), "₹%.2f", totalAmount);
    }

    /**
     * Check if order can be cancelled (not completed or already cancelled)
     */
    public boolean canBeCancelled() {
        return !STATUS_DELIVERED.equalsIgnoreCase(orderStatus) && 
               !STATUS_CANCELLED.equalsIgnoreCase(orderStatus);
    }

    /**
     * Check if order is delivered (completed) - can provide feedback
     */
    public boolean isDelivered() {
        return STATUS_DELIVERED.equalsIgnoreCase(orderStatus);
    }

    /**
     * Get status display text
     */
    public String getStatusDisplayText() {
        if (orderStatus == null) return "Unknown";
        switch (orderStatus.toUpperCase()) {
            case STATUS_PLACED: return "Placed";
            case STATUS_PACKING: return "Packing";
            case STATUS_READY: return "Ready";
            case STATUS_DELIVERED: return "Completed";
            case STATUS_CANCELLED: return "Cancelled";
            default: return orderStatus;
        }
    }

    /**
     * Get status color for badge
     * Returns: 0xFF39FF14 (green) for completed, 0xFFFF6B35 (orange) for active, 0xFFFF4444 (red) for cancelled
     */
    public int getStatusColor() {
        if (orderStatus == null) return 0xFFFF6B35;
        switch (orderStatus.toUpperCase()) {
            case STATUS_DELIVERED: return 0xFF39FF14; // Green
            case STATUS_CANCELLED: return 0xFFFF4444; // Red
            default: return 0xFFFF6B35; // Orange for active states
        }
    }
}
