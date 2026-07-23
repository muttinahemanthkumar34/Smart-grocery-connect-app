package com.SIMATS.Groceryconnect.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Product;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.CartManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RecyclerView adapter for displaying products in a grid.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private List<Product> productListFull; // For filtering
    private OnCartUpdateListener cartListener;
    private int userId;
    private RequestQueue requestQueue;
    private static final String TAG = "ProductAdapter";

    public interface OnCartUpdateListener {
        void onCartUpdated(int totalItems);
    }

    public ProductAdapter(List<Product> productList, OnCartUpdateListener listener, int userId, Context context) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.cartListener = listener;
        this.userId = userId;
        this.requestQueue = Volley.newRequestQueue(context);
    }
    
    // Legacy constructor for backward compatibility
    public ProductAdapter(List<Product> productList, OnCartUpdateListener listener) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.cartListener = listener;
        this.userId = 0;
        this.requestQueue = null;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * Update products list
     */
    public void updateProducts(List<Product> newProducts) {
        this.productList.clear();
        this.productList.addAll(newProducts);
        this.productListFull = new ArrayList<>(newProducts);
        notifyDataSetChanged();
    }

    /**
     * Filter products by search query
     */
    public void filter(String query) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(productListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Product product : productListFull) {
                if (product.getProductName().toLowerCase().contains(lowerQuery)) {
                    productList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filter products by category
     */
    public void filterByCategory(String category) {
        productList.clear();
        if (category == null || category.equalsIgnoreCase("All")) {
            productList.addAll(productListFull);
        } else {
            for (Product product : productListFull) {
                if (product.getCategory().equalsIgnoreCase(category)) {
                    productList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Get total cart items count
     */
    public int getTotalCartItems() {
        int total = 0;
        for (Product product : productListFull) {
            total += product.getCartQuantity();
        }
        return total;
    }

    /**
     * Notify cart listener of updates
     */
    private void notifyCartUpdate() {
        if (cartListener != null) {
            cartListener.onCartUpdated(getTotalCartItems());
        }
    }

    /**
     * ViewHolder for product items
     */
    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private ImageView ivSave;
        private TextView tvProductName, tvPrice;
        private TextView btnAddToCart;
        private LinearLayout layoutQuantity;
        private TextView btnDecrease, tvQuantity, btnIncrease;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivSave = itemView.findViewById(R.id.iv_save);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            layoutQuantity = itemView.findViewById(R.id.layout_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
        }

        void bind(Product product) {
            tvProductName.setText(product.getProductName());
            tvPrice.setText(String.format("₹%.2f", product.getPrice()));

            // Load product image from database
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String fullUrl = imageUrl.startsWith("http") ? imageUrl : ApiConfig.IMAGE_BASE_URL + imageUrl;
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Show cart state
            updateCartUI(product);

            // Add to cart click
            btnAddToCart.setOnClickListener(v -> {
                product.setCartQuantity(1);
                CartManager.getInstance().addToCart(product, 1);
                updateCartUI(product);
                notifyCartUpdate();
            });

            // Increase quantity
            btnIncrease.setOnClickListener(v -> {
                if (product.getCartQuantity() < product.getStockQuantity()) {
                    int newQty = product.getCartQuantity() + 1;
                    product.setCartQuantity(newQty);
                    CartManager.getInstance().updateQuantity(product.getProductId(), newQty);
                    updateCartUI(product);
                    notifyCartUpdate();
                }
            });

            // Decrease quantity
            btnDecrease.setOnClickListener(v -> {
                int newQty = product.getCartQuantity() - 1;
                product.setCartQuantity(newQty);
                if (newQty <= 0) {
                    CartManager.getInstance().removeFromCart(product.getProductId());
                } else {
                    CartManager.getInstance().updateQuantity(product.getProductId(), newQty);
                }
                updateCartUI(product);
                notifyCartUpdate();
            });
            
            // Update save icon state
            updateSaveIcon(product);
            
            // Save icon click
            if (ivSave != null) {
                ivSave.setOnClickListener(v -> {
                    Log.d(TAG, "Save icon clicked! userId=" + userId + ", productId=" + product.getProductId() + ", shopId=" + product.getShopId());
                    if (userId <= 0) {
                        Log.e(TAG, "userId is invalid: " + userId);
                        Toast.makeText(itemView.getContext(), "Please log in to save items", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (requestQueue == null) {
                        Log.e(TAG, "requestQueue is null");
                        Toast.makeText(itemView.getContext(), "Failed to save item", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    toggleSaveState(product);
                });
            } else {
                Log.e(TAG, "ivSave is null!");
            }
        }
        
        private void updateSaveIcon(Product product) {
            if (ivSave != null) {
                if (product.isSaved()) {
                    ivSave.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.accent_green));
                } else {
                    ivSave.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                }
            }
        }
        
        private void toggleSaveState(Product product) {
            String url = ApiConfig.SAVE_ITEM_URL;
            Log.d(TAG, "toggleSaveState: Making request to " + url);
            Log.d(TAG, "toggleSaveState: params - user_id=" + userId + ", product_id=" + product.getProductId() + ", shop_id=" + product.getShopId());
            
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        Log.d(TAG, "toggleSaveState: Response received: " + response);
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                boolean saved = json.getBoolean("saved");
                                product.setSaved(saved);
                                updateSaveIcon(product);
                                String message = saved ? "Item saved" : "Item removed from saved";
                                Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "toggleSaveState: API returned success=false: " + json.optString("message"));
                                Toast.makeText(itemView.getContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing save response: " + e.getMessage());
                            Toast.makeText(itemView.getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        String errorMsg = error.getMessage();
                        if (error.networkResponse != null) {
                            errorMsg += " Status: " + error.networkResponse.statusCode;
                        }
                        Log.e(TAG, "Error saving item: " + errorMsg);
                        Toast.makeText(itemView.getContext(), "Network error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", String.valueOf(userId));
                    params.put("product_id", String.valueOf(product.getProductId()));
                    params.put("shop_id", String.valueOf(product.getShopId()));
                    Log.d(TAG, "getParams called, returning: " + params.toString());
                    return params;
                }
            };
            
            Log.d(TAG, "Adding request to queue");
            requestQueue.add(request);
        }

        private void updateCartUI(Product product) {
            if (product.isInCart()) {
                btnAddToCart.setVisibility(View.GONE);
                layoutQuantity.setVisibility(View.VISIBLE);
                tvQuantity.setText(String.valueOf(product.getCartQuantity()));
            } else {
                btnAddToCart.setVisibility(View.VISIBLE);
                layoutQuantity.setVisibility(View.GONE);
            }
        }
    }
}
