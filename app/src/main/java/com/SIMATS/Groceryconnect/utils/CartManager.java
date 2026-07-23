package com.SIMATS.Groceryconnect.utils;

import com.SIMATS.Groceryconnect.models.CartItem;
import com.SIMATS.Groceryconnect.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage cart items across activities.
 */
public class CartManager {
    private static CartManager instance;
    
    private List<CartItem> cartItems;
    private int shopId;
    private String shopName;
    private String shopCity;
    private boolean deliveryAvailable;
    private String shopImage;
    
    private CartManager() {
        cartItems = new ArrayList<>();
    }
    
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }
    
    // Shop info
    public void setShopInfo(int shopId, String shopName, String shopCity, boolean deliveryAvailable, String shopImage) {
        // If different shop with existing items, clear cart first
        if (this.shopId != 0 && this.shopId != shopId && !cartItems.isEmpty()) {
            cartItems.clear();
        }
        this.shopId = shopId;
        this.shopName = shopName;
        this.shopCity = shopCity;
        this.deliveryAvailable = deliveryAvailable;
        this.shopImage = shopImage;
    }
    
    public int getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getShopCity() { return shopCity; }
    public boolean isDeliveryAvailable() { return deliveryAvailable; }
    public String getShopImage() { return shopImage; }
    
    // Cart operations
    public void addToCart(Product product, int quantity) {
        // Check if product already in cart
        for (CartItem item : cartItems) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // New item
        cartItems.add(new CartItem(product, quantity));
    }
    
    public void updateQuantity(int productId, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProduct().getProductId() == productId) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                } else {
                    cartItems.get(i).setQuantity(quantity);
                }
                return;
            }
        }
    }
    
    public void removeFromCart(int productId) {
        cartItems.removeIf(item -> item.getProduct().getProductId() == productId);
    }
    
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
    
    public int getCartItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
    
    public double getSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }
    
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
    
    public void clearCart() {
        cartItems.clear();
        shopId = 0;
        shopName = null;
        shopCity = null;
    }
    
    // Check if product is in cart
    public int getProductQuantity(int productId) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getProductId() == productId) {
                return item.getQuantity();
            }
        }
        return 0;
    }
}
