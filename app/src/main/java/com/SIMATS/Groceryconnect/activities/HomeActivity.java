package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.ShopAdapter;
import com.SIMATS.Groceryconnect.models.Shop;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity - User Dashboard showing nearby grocery stores.
 */
public class HomeActivity extends AppCompatActivity implements ShopAdapter.OnShopClickListener {

    private static final String TAG = "HomeActivity";

    private EditText etSearch;
    private RecyclerView rvStores;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private LinearLayout navHome, navOrders, navAi, navCartText, navProfile;

    private SessionManager sessionManager;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList = new ArrayList<>();
    private String userCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        Log.d(TAG, "onCreate: HomeActivity started");

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity");
            navigateToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        setupBottomNav();
        
        // First fetch user's default address city, then fetch shops
        fetchUserDefaultAddress();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        rvStores = findViewById(R.id.rv_stores);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tv_empty);
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navAi = findViewById(R.id.nav_ai);
        navCartText = findViewById(R.id.nav_cart_text);
        navProfile = findViewById(R.id.nav_profile);
        Log.d(TAG, "initViews: All views initialized");
    }

    private void setupRecyclerView() {
        shopAdapter = new ShopAdapter(shopList, this);
        rvStores.setLayoutManager(new LinearLayoutManager(this));
        rvStores.setAdapter(shopAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                shopAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            // Already on home
        });
        
        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserOrderHistoryActivity.class);
            startActivity(intent);
        });
        
        // AI navigation
        navAi.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiAssistantActivity.class);
            startActivity(intent);
        });
        
        // Cart navigation - check if cart has items
        navCartText.setOnClickListener(v -> {
            com.SIMATS.Groceryconnect.utils.CartManager cartManager = 
                com.SIMATS.Groceryconnect.utils.CartManager.getInstance();
            if (cartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, CartDetailsActivity.class);
                startActivity(intent);
            }
        });
        
        navProfile.setOnClickListener(v -> {
            // Navigate to profile
            Intent intent = new Intent(this, UserProfile.class);
            startActivity(intent);
        });
    }

    private void fetchUserDefaultAddress() {
        showLoading(true);
        int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_USER_DEFAULT_ADDRESS_URL + "?user_id=" + userId;
        
        Log.d(TAG, "fetchUserDefaultAddress: Fetching from " + url);
        
        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                Log.d(TAG, "fetchUserDefaultAddress: Response=" + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        userCity = json.getString("city");
                        Log.d(TAG, "fetchUserDefaultAddress: City=" + userCity);
                        fetchShopsByCity(userCity);
                    } else {
                        // No default address, try fetching all shops or prompt user
                        showLoading(false);
                        tvEmpty.setText("Please set your address to see nearby shops");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "fetchUserDefaultAddress: JSON error", e);
                    showLoading(false);
                    // Fallback: fetch all shops or use default city
                    fetchShopsByCity("chennai");
                }
            },
            error -> {
                Log.e(TAG, "fetchUserDefaultAddress: Network error", error);
                showLoading(false);
                // Fallback: try with default city
                fetchShopsByCity("chennai");
            }
        );
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchShopsByCity(String city) {
        showLoading(true);
        String url = ApiConfig.GET_SHOPS_BY_CITY_URL + "?city=" + city;
        
        Log.d(TAG, "fetchShopsByCity: Fetching from " + url);
        
        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "fetchShopsByCity: Response=" + response);
                parseShopsResponse(response);
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "fetchShopsByCity: Network error", error);
                Toast.makeText(this, "Failed to load shops", Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        );
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseShopsResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            if (json.getBoolean("status")) {
                JSONArray shopsArray = json.getJSONArray("shops");
                List<Shop> shops = new ArrayList<>();
                
                for (int i = 0; i < shopsArray.length(); i++) {
                    JSONObject shopObj = shopsArray.getJSONObject(i);
                    Shop shop = new Shop(
                        shopObj.getInt("shop_id"),
                        shopObj.getString("shop_name"),
                        shopObj.getString("category"),
                        shopObj.optString("opening_time", "09:00"),
                        shopObj.optString("closing_time", "21:00"),
                        shopObj.optInt("delivery_available", 0) == 1
                    );
                    // Set additional properties for ShopDetails
                    shop.setShopAddress(shopObj.optString("shop_address", ""));
                    shop.setCity(shopObj.optString("city", ""));
                    shop.setShopPhone(shopObj.optString("shop_phone", ""));
                    shop.setPincode(shopObj.optString("pincode", ""));
                    shop.setShopImage(shopObj.optString("shop_image", ""));
                    shop.setOnline(shopObj.optInt("is_online", 1) == 1);
                    // Parse rating from backend (calculated average from feedbacks)
                    shop.setRating(shopObj.optDouble("avg_rating", 0.0));
                    shop.setRatingCount(shopObj.optInt("rating_count", 0));
                    shops.add(shop);
                }
                
                Log.d(TAG, "parseShopsResponse: Found " + shops.size() + " shops");
                shopAdapter.updateShops(shops);
            } else {
                Log.w(TAG, "parseShopsResponse: No shops found");
            }
            updateEmptyState();
        } catch (JSONException e) {
            Log.e(TAG, "parseShopsResponse: JSON error", e);
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (shopAdapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvStores.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvStores.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onShopClick(Shop shop) {
        Log.d(TAG, "onShopClick: " + shop.getShopName());
        // Navigate to Shop Details
        Intent intent = new Intent(this, ShopDetails.class);
        intent.putExtra(ShopDetails.EXTRA_SHOP_ID, shop.getShopId());
        intent.putExtra(ShopDetails.EXTRA_SHOP_NAME, shop.getShopName());
        intent.putExtra(ShopDetails.EXTRA_SHOP_ADDRESS, shop.getShopAddress());
        intent.putExtra(ShopDetails.EXTRA_SHOP_CITY, shop.getCity());
        intent.putExtra(ShopDetails.EXTRA_OPENING_TIME, shop.getOpeningTime());
        intent.putExtra(ShopDetails.EXTRA_CLOSING_TIME, shop.getClosingTime());
        intent.putExtra(ShopDetails.EXTRA_DELIVERY_AVAILABLE, shop.isDeliveryAvailable());
        intent.putExtra(ShopDetails.EXTRA_SHOP_PHONE, shop.getShopPhone());
        intent.putExtra(ShopDetails.EXTRA_PINCODE, shop.getPincode());
        intent.putExtra(ShopDetails.EXTRA_SHOP_IMAGE, shop.getShopImage());
        intent.putExtra(ShopDetails.EXTRA_IS_ONLINE, shop.isOnline());
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
