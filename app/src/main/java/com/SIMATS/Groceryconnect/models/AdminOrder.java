package com.SIMATS.Groceryconnect.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Model class for admin orders management.
 */
public class AdminOrder {
    private int orderId;
    private String userName;
    private String userPhone;
    private String deliveryAddress;
    private String orderType;
    private double totalAmount;
    private String orderStatus;
    private String createdAt;
    private List<OrderItem> items;

    // Status constants
    public static final String STATUS_PLACED = "PLACED";
    public static final String STATUS_PACKING = "PACKING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public AdminOrder() {}

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    /**
     * Get formatted order ID for display
     */
    public String getFormattedOrderId() {
        return "Order #GC-" + orderId;
    }

    /**
     * Get formatted amount with Rupee symbol
     */
    public String getFormattedAmount() {
        return String.format(Locale.getDefault(), "₹%.2f", totalAmount);
    }

    /**
     * Get formatted date string
     */
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return date != null ? outputFormat.format(date) : createdAt;
        } catch (ParseException e) {
            return createdAt;
        }
    }

    /**
     * Check if order is pending (new order awaiting acceptance)
     */
    public boolean isPending() {
        return STATUS_PLACED.equalsIgnoreCase(orderStatus);
    }

    /**
     * Check if order is completed (delivered or cancelled)
     */
    public boolean isCompleted() {
        return STATUS_DELIVERED.equalsIgnoreCase(orderStatus) || 
               STATUS_CANCELLED.equalsIgnoreCase(orderStatus);
    }

    /**
     * Check if order is in progress (accepted but not completed)
     */
    public boolean isInProgress() {
        return STATUS_PACKING.equalsIgnoreCase(orderStatus) ||
               STATUS_READY.equalsIgnoreCase(orderStatus) ||
               STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(orderStatus);
    }

    /**
     * Check if order is delivery type
     */
    public boolean isDeliveryOrder() {
        return "delivery".equalsIgnoreCase(orderType);
    }

    /**
     * Get the next status in the workflow
     */
    public String getNextStatus() {
        if (orderStatus == null) return STATUS_PACKING;
        
        switch (orderStatus.toUpperCase()) {
            case STATUS_PLACED:
                return STATUS_PACKING;
            case STATUS_PACKING:
                return STATUS_READY;
            case STATUS_READY:
                return isDeliveryOrder() ? STATUS_OUT_FOR_DELIVERY : STATUS_DELIVERED;
            case STATUS_OUT_FOR_DELIVERY:
                return STATUS_DELIVERED;
            default:
                return null;
        }
    }

    /**
     * Get button text for next status action
     */
    public String getNextStatusButtonText() {
        String nextStatus = getNextStatus();
        if (nextStatus == null) return null;
        
        switch (nextStatus) {
            case STATUS_PACKING:
                return "Start Packing";
            case STATUS_READY:
                return "Mark Ready";
            case STATUS_OUT_FOR_DELIVERY:
                return "Out for Delivery";
            case STATUS_DELIVERED:
                return isDeliveryOrder() ? "Mark Delivered" : "Order Picked Up";
            default:
                return "Update Status";
        }
    }

    /**
     * Get status display text
     */
    public String getStatusDisplayText() {
        if (orderStatus == null) return "Unknown";
        switch (orderStatus.toUpperCase()) {
            case STATUS_PLACED: return "Pending";
            case STATUS_PACKING: return "Packing";
            case STATUS_READY: return "Ready";
            case STATUS_OUT_FOR_DELIVERY: return "Out for Delivery";
            case STATUS_DELIVERED: return "Delivered";
            case STATUS_CANCELLED: return "Cancelled";
            default: return orderStatus;
        }
    }

    /**
     * Get items formatted as string for display
     */
    public String getItemsDisplayText() {
        if (items == null || items.isEmpty()) return "No items";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            sb.append(item.getQuantity()).append("x - ").append(item.getProductName());
            if (i < items.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Inner class for order items
     */
    public static class OrderItem {
        private String productName;
        private int quantity;
        private double price;

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public String getFormattedPrice() {
            return String.format(Locale.getDefault(), "₹%.2f", price);
        }
    }
}
