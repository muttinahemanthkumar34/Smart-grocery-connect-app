package com.SIMATS.Groceryconnect.models;

/**
 * Shop model class representing a grocery store.
 */
public class Shop {
    private int shopId;
    private String shopName;
    private String category;
    private String shopAddress;
    private String city;
    private String pincode;
    private String shopPhone;
    private String openingTime;
    private String closingTime;
    private boolean deliveryAvailable;
    private boolean isOnline;
    private double deliveryRadius;
    private double rating;
    private int ratingCount;
    private String shopImage;

    // Constructor
    public Shop(int shopId, String shopName, String category, String openingTime, 
                String closingTime, boolean deliveryAvailable) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.category = category;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.deliveryAvailable = deliveryAvailable;
        this.rating = 0; // Default - will be populated from database
        this.ratingCount = 0;
        this.isOnline = true; // Default to online
    }

    // Empty constructor for JSON parsing
    public Shop() {}

    // Getters and Setters
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getShopAddress() { return shopAddress; }
    public void setShopAddress(String shopAddress) { this.shopAddress = shopAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getShopPhone() { return shopPhone; }
    public void setShopPhone(String shopPhone) { this.shopPhone = shopPhone; }

    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }

    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }

    public boolean isDeliveryAvailable() { return deliveryAvailable; }
    public void setDeliveryAvailable(boolean deliveryAvailable) { this.deliveryAvailable = deliveryAvailable; }

    public double getDeliveryRadius() { return deliveryRadius; }
    public void setDeliveryRadius(double deliveryRadius) { this.deliveryRadius = deliveryRadius; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { this.isOnline = online; }

    public String getShopImage() { return shopImage; }
    public void setShopImage(String shopImage) { this.shopImage = shopImage; }

    /**
     * Check if shop is currently open based on opening/closing times.
     */
    public boolean isOpen() {
        // Shop is open if it's online (controlled by admin)
        return isOnline;
    }
}
