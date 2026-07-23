package com.SIMATS.Groceryconnect.models;

/**
 * Product model class representing a product in a shop.
 */
public class Product {
    private int productId;
    private int shopId;
    private String productName;
    private String category;
    private double price;
    private int stockQuantity;
    private String imageUrl;
    
    // Cart quantity (not from DB, used for cart operations)
    private int cartQuantity = 0;

    // Empty constructor for JSON parsing
    public Product() {}

    // Constructor
    public Product(int productId, String productName, String category, double price, int stockQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getCartQuantity() { return cartQuantity; }
    public void setCartQuantity(int cartQuantity) { this.cartQuantity = cartQuantity; }

    public boolean isInCart() { return cartQuantity > 0; }
    
    // Saved state (for bookmark functionality)
    private boolean isSaved = false;
    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { this.isSaved = saved; }
    
    // Shop name (for saved items display)
    private String shopName;
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
}
