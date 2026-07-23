package com.SIMATS.Groceryconnect.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.SavedItemsAdapter;
import com.SIMATS.Groceryconnect.models.Product;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display user's saved items
 */
public class SavedItemsActivity extends AppCompatActivity implements SavedItemsAdapter.OnItemRemovedListener {

    private static final String TAG = "SavedItemsActivity";
    
    private ImageView ivBack;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private RecyclerView rvSavedItems;
    
    private SavedItemsAdapter adapter;
    private List<Product> savedItems;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);
        
        initViews();
        setupListeners();
        loadSavedItems();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layout_empty);
        rvSavedItems = findViewById(R.id.rv_saved_items);
        
        requestQueue = Volley.newRequestQueue(this);
        sessionManager = new SessionManager(this);
        
        savedItems = new ArrayList<>();
        adapter = new SavedItemsAdapter(savedItems, this);
        rvSavedItems.setLayoutManager(new LinearLayoutManager(this));
        rvSavedItems.setAdapter(adapter);
    }
    
    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
    }
    
    private void loadSavedItems() {
        progressBar.setVisibility(View.VISIBLE);
        rvSavedItems.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        
        int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_SAVED_ITEMS_URL + "?user_id=" + userId;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray items = response.getJSONArray("saved_items");
                            savedItems.clear();
                            
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                Product product = new Product();
                                product.setProductId(item.getInt("product_id"));
                                product.setShopId(item.getInt("shop_id"));
                                product.setProductName(item.getString("product_name"));
                                product.setPrice(item.getDouble("price"));
                                product.setStockQuantity(item.getInt("stock_quantity"));
                                product.setCategory(item.getString("category"));
                                product.setImageUrl(item.optString("image_url", ""));
                                product.setShopName(item.getString("shop_name"));
                                product.setSaved(true);
                                savedItems.add(product);
                            }
                            
                            adapter.notifyDataSetChanged();
                            
                            if (savedItems.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvSavedItems.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvSavedItems.setVisibility(View.VISIBLE);
                            }
                        } else {
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing saved items: " + e.getMessage());
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error loading saved items: " + error.getMessage());
                    Toast.makeText(this, "Failed to load saved items", Toast.LENGTH_SHORT).show();
                });
        
        requestQueue.add(request);
    }
    
    @Override
    public void onItemRemoved(Product product, int position) {
        // Remove from server
        removeFromSaved(product, position);
    }
    
    private void removeFromSaved(Product product, int position) {
        int userId = sessionManager.getUserId();
        String url = ApiConfig.SAVE_ITEM_URL;
        
        Log.d(TAG, "Removing saved item: userId=" + userId + ", productId=" + product.getProductId() + ", shopId=" + product.getShopId());
        
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Remove response: " + response);
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.getBoolean("success")) {
                            savedItems.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, savedItems.size());
                            
                            if (savedItems.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvSavedItems.setVisibility(View.GONE);
                            }
                            
                            Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Error removing item: " + error.getMessage());
                    Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("product_id", String.valueOf(product.getProductId()));
                params.put("shop_id", String.valueOf(product.getShopId()));
                return params;
            }
        };
        
        requestQueue.add(request);
    }
}
