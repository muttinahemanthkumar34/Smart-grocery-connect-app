package com.SIMATS.Groceryconnect.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.NotificationAdapter;
import com.SIMATS.Groceryconnect.adapters.ShopAdapter;
import com.SIMATS.Groceryconnect.models.Notification;
import com.SIMATS.Groceryconnect.models.Shop;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboard extends AppCompatActivity implements ShopAdapter.OnShopClickListener, NotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "UserDashboard";
    private static final long NOTIFICATION_POLL_INTERVAL = 2000; // 2 seconds for near-instant updates

    private EditText etSearch;
    private RecyclerView rvStores;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FrameLayout layoutNotification;
    private TextView tvNotificationBadge;

    private ShopAdapter shopAdapter;
    private List<Shop> shopList;
    private RequestQueue requestQueue;
    private String userCity;
    private int userId;
    private boolean isLoadingFromNewIntent = false;
    private boolean isFirstLoad = true;
    private SessionManager sessionManager;
    
    // Notification polling
    private Handler notificationHandler;
    private Runnable notificationRunnable;
    private int unreadCount = 0;
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        
        Log.d(TAG, "onCreate: userId = " + userId);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupBottomNavigation();
        setupNotificationIcon();
        setupNotificationPolling();

        if (savedInstanceState == null) {
            loadUserCity();
        }
    }
    
    private void setupBottomNavigation() {
        findViewById(R.id.nav_orders).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserOrderHistoryActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.nav_ai).setOnClickListener(v -> {
            Intent intent = new Intent(this, AiAssistantActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.nav_cart_text).setOnClickListener(v -> {
            com.SIMATS.Groceryconnect.utils.CartManager cartManager = 
                com.SIMATS.Groceryconnect.utils.CartManager.getInstance();
            if (cartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, CartDetailsActivity.class);
                startActivity(intent);
            }
        });
        
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfile.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        rvStores = findViewById(R.id.rv_stores);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tv_empty);
        layoutNotification = findViewById(R.id.layout_notification);
        tvNotificationBadge = findViewById(R.id.tv_notification_badge);

        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupRecyclerView() {
        shopList = new ArrayList<>();
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
    
    private void setupNotificationIcon() {
        layoutNotification.setOnClickListener(v -> showNotificationDialog());
    }
    
    private void setupNotificationPolling() {
        notificationHandler = new Handler(Looper.getMainLooper());
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                loadNotificationCount();
                notificationHandler.postDelayed(this, NOTIFICATION_POLL_INTERVAL);
            }
        };
    }
    
    private void loadNotificationCount() {
        if (!sessionManager.isNotificationsEnabled()) {
            updateBadge(0);
            return;
        }
        
        String url = ApiConfig.GET_NOTIFICATIONS_URL + "?user_id=" + userId;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    if (response.getBoolean("status")) {
                        unreadCount = response.getInt("unread_count");
                        updateBadge(unreadCount);
                        
                        // Parse notifications for cache
                        JSONArray notificationsArray = response.getJSONArray("notifications");
                        notificationList.clear();
                        for (int i = 0; i < notificationsArray.length(); i++) {
                            JSONObject obj = notificationsArray.getJSONObject(i);
                            Notification notification = new Notification();
                            notification.setId(obj.getInt("id"));
                            notification.setOrderId(obj.isNull("order_id") ? null : obj.getInt("order_id"));
                            notification.setTitle(obj.getString("title"));
                            notification.setMessage(obj.getString("message"));
                            notification.setRead(obj.getBoolean("is_read"));
                            notification.setCreatedAt(obj.getString("created_at"));
                            notification.setOrderStatus(obj.optString("order_status", ""));
                            notificationList.add(notification);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing notifications", e);
                }
            },
            error -> Log.e(TAG, "Error loading notifications", error));
        
        requestQueue.add(request);
    }
    
    private void updateBadge(int count) {
        if (count > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }
    
    private void showNotificationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notifications);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Set dialog width to 90% of screen
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }
        
        RecyclerView rvNotifications = dialog.findViewById(R.id.rv_notifications);
        View layoutEmpty = dialog.findViewById(R.id.layout_empty);
        ProgressBar progressBarDialog = dialog.findViewById(R.id.progress_bar);
        TextView tvMarkAllRead = dialog.findViewById(R.id.tv_mark_all_read);
        
        NotificationAdapter adapter = new NotificationAdapter(notification -> {
            dialog.dismiss();
            onNotificationClick(notification);
        });
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
        
        // Show cached notifications
        if (notificationList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(notificationList);
        }
        
        // Mark all read button
        tvMarkAllRead.setOnClickListener(v -> {
            markAllNotificationsRead();
            adapter.markAllAsRead();
            updateBadge(0);
        });
        
        // Clear all button
        TextView tvClearAll = dialog.findViewById(R.id.tv_clear_all);
        tvClearAll.setOnClickListener(v -> {
            clearAllNotifications();
            notificationList.clear();
            adapter.setNotifications(notificationList);
            layoutEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            updateBadge(0);
            Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
        });
        
        dialog.show();
    }
    
    private void clearAllNotifications() {
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.CLEAR_NOTIFICATIONS_URL,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        unreadCount = 0;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing clear response", e);
                }
            },
            error -> Log.e(TAG, "Error clearing notifications", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };
        
        requestQueue.add(request);
    }
    
    private void markAllNotificationsRead() {
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.MARK_NOTIFICATION_READ_URL,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        for (Notification n : notificationList) {
                            n.setRead(true);
                        }
                        unreadCount = 0;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing mark read response", e);
                }
            },
            error -> Log.e(TAG, "Error marking notifications read", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("mark_all", "true");
                return params;
            }
        };
        
        requestQueue.add(request);
    }
    
    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead()) {
            markNotificationRead(notification.getId());
            notification.setRead(true);
            if (unreadCount > 0) {
                unreadCount--;
                updateBadge(unreadCount);
            }
        }
        
        // Navigate to order details if this is an order notification
        if (notification.getOrderId() != null) {
            Intent intent = new Intent(this, UserOrderDetailsActivity.class);
            intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_ID, notification.getOrderId().intValue());
            intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_STATUS, notification.getOrderStatus());
            startActivity(intent);
        }
    }
    
    private void markNotificationRead(int notificationId) {
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.MARK_NOTIFICATION_READ_URL,
            response -> Log.d(TAG, "Notification marked as read"),
            error -> Log.e(TAG, "Error marking notification read", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("notification_id", String.valueOf(notificationId));
                return params;
            }
        };
        
        requestQueue.add(request);
    }

    private void loadUserCity() {
        showLoading(true);

        String url = ApiConfig.GET_USER_DEFAULT_ADDRESS_URL + "?user_id=" + userId;
        Log.d(TAG, "loadUserCity: Requesting URL = " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                Log.d(TAG, "loadUserCity: Response = " + response.toString());
                try {
                    if (response.getBoolean("status")) {
                        userCity = response.getString("city");
                        Log.d(TAG, "loadUserCity: Got city = " + userCity);
                    } else {
                        Log.d(TAG, "loadUserCity: No address found, using default city Chennai");
                        userCity = "Chennai";
                    }
                    loadShopsByCity();
                } catch (Exception e) {
                    Log.e(TAG, "loadUserCity: Error parsing response", e);
                    showLoading(false);
                    showError("Error loading address: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "loadUserCity: Network error", error);
                showLoading(false);
                userCity = "Chennai";
                loadShopsByCity();
            });

        requestQueue.add(request);
    }

    private void loadShopsByCity() {
        String url = ApiConfig.GET_SHOPS_BY_CITY_URL + "?city=" + userCity;
        Log.d(TAG, "loadShopsByCity: Requesting URL = " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                showLoading(false);
                Log.d(TAG, "loadShopsByCity: Response = " + response.toString());
                try {
                    if (response.getBoolean("status")) {
                        JSONArray shopsArray = response.getJSONArray("shops");
                        Log.d(TAG, "loadShopsByCity: Found " + shopsArray.length() + " shops");
                        shopList.clear();

                        for (int i = 0; i < shopsArray.length(); i++) {
                            JSONObject shopJson = shopsArray.getJSONObject(i);
                            Shop shop = new Shop();
                            shop.setShopId(shopJson.getInt("shop_id"));
                            shop.setShopName(shopJson.getString("shop_name"));
                            shop.setCategory(shopJson.optString("category", "General"));
                            shop.setShopAddress(shopJson.optString("shop_address", ""));
                            shop.setCity(shopJson.optString("city", ""));
                            shop.setPincode(shopJson.optString("pincode", ""));
                            shop.setShopPhone(shopJson.optString("shop_phone", ""));
                            shop.setOpeningTime(shopJson.optString("opening_time", "09:00"));
                            shop.setClosingTime(shopJson.optString("closing_time", "21:00"));
                            shop.setDeliveryAvailable(shopJson.optInt("delivery_available", 0) == 1);
                            shop.setOnline(shopJson.optInt("is_online", 1) == 1);
                            shop.setDeliveryRadius(shopJson.optDouble("delivery_radius", 5.0));
                            shop.setRating(shopJson.optDouble("avg_rating", 0.0));
                            shop.setRatingCount(shopJson.optInt("rating_count", 0));
                            shop.setShopImage(shopJson.optString("shop_image", ""));

                            shopList.add(shop);
                        }

                        shopAdapter.updateShops(shopList);
                        updateEmptyState();

                    } else {
                        Log.d(TAG, "loadShopsByCity: No shops found - status false");
                        updateEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "loadShopsByCity: Error parsing response", e);
                    showError("Error parsing shops: " + e.getMessage());
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "loadShopsByCity: Network error", error);
                showError("Network error: " + error.getMessage());
                updateEmptyState();
            });

        requestQueue.add(request);
    }

    @Override
    public void onShopClick(Shop shop) {
        Intent intent = new Intent(this, ShopDetails.class);
        intent.putExtra(ShopDetails.EXTRA_SHOP_ID, shop.getShopId());
        intent.putExtra(ShopDetails.EXTRA_SHOP_NAME, shop.getShopName());
        intent.putExtra(ShopDetails.EXTRA_SHOP_ADDRESS, shop.getShopAddress());
        intent.putExtra(ShopDetails.EXTRA_SHOP_CITY, shop.getCity());
        intent.putExtra(ShopDetails.EXTRA_OPENING_TIME, shop.getOpeningTime());
        intent.putExtra(ShopDetails.EXTRA_CLOSING_TIME, shop.getClosingTime());
        intent.putExtra(ShopDetails.EXTRA_DELIVERY_AVAILABLE, shop.isDeliveryAvailable());
        intent.putExtra(ShopDetails.EXTRA_SHOP_PHONE, shop.getShopPhone());
        intent.putExtra(ShopDetails.EXTRA_SHOP_IMAGE, shop.getShopImage());
        intent.putExtra(ShopDetails.EXTRA_IS_ONLINE, shop.isOnline());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvStores.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (shopList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvStores.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvStores.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Start notification polling
        if (notificationHandler != null) {
            notificationHandler.post(notificationRunnable);
        }
        
        if (isFirstLoad) {
            isFirstLoad = false;
            return;
        }
        if (!isLoadingFromNewIntent) {
            loadUserCity();
        }
        isLoadingFromNewIntent = false;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop notification polling when not visible
        if (notificationHandler != null) {
            notificationHandler.removeCallbacks(notificationRunnable);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        isLoadingFromNewIntent = true;
        loadUserCity();
    }
}