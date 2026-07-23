package com.SIMATS.Groceryconnect.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.AdminNotificationAdapter;
import com.SIMATS.Groceryconnect.models.Notification;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends AppCompatActivity implements AdminNotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "AdminDashboard";
    private static final long NOTIFICATION_POLL_INTERVAL = 2000; // 2 seconds for near-instant updates

    // Views
    private TextView tvStoreName, tvOnlineStatus;
    private TextView tvTodayOrders, tvPendingOrders, tvCompletedOrders, tvCancelledOrders;
    private TextView tvTodayEarnings;
    private SwitchCompat switchOnline, switchDelivery;
    private View viewOnlineDot;
    private ProgressBar progressBar;
    private FrameLayout layoutNotification;
    private TextView tvNotificationBadge;

    // Quick Actions
    private LinearLayout btnManageStock, btnPastOrders, btnEditShop;

    // Bottom Nav
    private LinearLayout navHome, navOrders, navProfile;

    // Data
    private SessionManager sessionManager;
    private int shopId = 0;
    
    // Notifications
    private Handler notificationHandler;
    private Runnable notificationRunnable;
    private int unreadCount = 0;
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        
        initViews();
        setupClickListeners();
        setupNotificationPolling();
        loadDashboardData();
    }

    private void initViews() {
        // Header
        tvStoreName = findViewById(R.id.tv_store_name);
        tvOnlineStatus = findViewById(R.id.tv_online_status);
        viewOnlineDot = findViewById(R.id.view_online_dot);
        progressBar = findViewById(R.id.progressBar);
        layoutNotification = findViewById(R.id.layout_notification);
        tvNotificationBadge = findViewById(R.id.tv_notification_badge);

        // Stats
        tvTodayOrders = findViewById(R.id.tv_today_orders);
        tvPendingOrders = findViewById(R.id.tv_pending_orders);
        tvCompletedOrders = findViewById(R.id.tv_completed_orders);
        tvCancelledOrders = findViewById(R.id.tv_cancelled_orders);

        // Toggles
        switchOnline = findViewById(R.id.switch_online);
        switchDelivery = findViewById(R.id.switch_delivery);

        // Today's Earnings
        tvTodayEarnings = findViewById(R.id.tv_today_earnings);

        // Quick Actions
        btnManageStock = findViewById(R.id.btn_manage_stock);
        btnPastOrders = findViewById(R.id.btn_past_orders);
        btnEditShop = findViewById(R.id.btn_edit_shop);

        // Bottom Nav
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navProfile = findViewById(R.id.nav_profile);
        
        // Notification click
        layoutNotification.setOnClickListener(v -> showNotificationDialog());
    }

    private void setupClickListeners() {
        // Online Status Toggle
        switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (shopId > 0) {
                updateShopStatus("is_online", isChecked ? 1 : 0);
                updateOnlineIndicator(isChecked);
            }
        });

        // Delivery Toggle
        switchDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (shopId > 0) {
                updateShopStatus("delivery_available", isChecked ? 1 : 0);
            }
        });

        // Quick Actions
        btnManageStock.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminStockManageActivity.class);
            startActivity(intent);
        });

        btnPastOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminOrderManagement.class);
            intent.putExtra("show_completed", true);
            startActivity(intent);
        });

        btnEditShop.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminProfile.class);
            intent.putExtra("open_edit_shop", true);
            startActivity(intent);
        });

        // Bottom Nav
        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminOrderManagement.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
    
    private void setupNotificationPolling() {
        notificationHandler = new Handler(Looper.getMainLooper());
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                loadAdminNotifications();
                notificationHandler.postDelayed(this, NOTIFICATION_POLL_INTERVAL);
            }
        };
    }
    
    private void loadAdminNotifications() {
        if (shopId <= 0) return;
        
        String url = ApiConfig.GET_ADMIN_NOTIFICATIONS_URL + "?shop_id=" + shopId;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    if (response.getBoolean("status")) {
                        unreadCount = response.getInt("unread_count");
                        updateBadge(unreadCount);
                        
                        JSONArray notificationsArray = response.getJSONArray("notifications");
                        notificationList.clear();
                        for (int i = 0; i < notificationsArray.length(); i++) {
                            JSONObject obj = notificationsArray.getJSONObject(i);
                            Notification notification = new Notification();
                            notification.setId(obj.getInt("id"));
                            notification.setOrderId(obj.isNull("order_id") ? null : obj.getInt("order_id"));
                            notification.setProductId(obj.isNull("product_id") ? null : obj.getInt("product_id"));
                            notification.setTitle(obj.getString("title"));
                            notification.setMessage(obj.getString("message"));
                            notification.setRead(obj.getBoolean("is_read"));
                            notification.setCreatedAt(obj.getString("created_at"));
                            notification.setOrderStatus(obj.optString("order_status", ""));
                            notificationList.add(notification);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing admin notifications", e);
                }
            },
            error -> Log.e(TAG, "Error loading admin notifications", error));
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
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
        dialog.setContentView(R.layout.dialog_admin_notifications);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }
        
        RecyclerView rvNotifications = dialog.findViewById(R.id.rv_notifications);
        View layoutEmpty = dialog.findViewById(R.id.layout_empty);
        TextView tvMarkAllRead = dialog.findViewById(R.id.tv_mark_all_read);
        TextView tvClearAll = dialog.findViewById(R.id.tv_clear_all);
        
        AdminNotificationAdapter adapter = new AdminNotificationAdapter(notification -> {
            dialog.dismiss();
            onNotificationClick(notification);
        });
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
        
        if (notificationList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(notificationList);
        }
        
        tvMarkAllRead.setOnClickListener(v -> {
            markAllAdminNotificationsRead();
            adapter.markAllAsRead();
            updateBadge(0);
        });
        
        tvClearAll.setOnClickListener(v -> {
            clearAllAdminNotifications();
            notificationList.clear();
            adapter.setNotifications(notificationList);
            layoutEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            updateBadge(0);
            Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
        });
        
        dialog.show();
    }
    
    private void markAllAdminNotificationsRead() {
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.MARK_ADMIN_NOTIFICATION_READ_URL,
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
            error -> Log.e(TAG, "Error marking admin notifications read", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shop_id", String.valueOf(shopId));
                params.put("mark_all", "true");
                return params;
            }
        };
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void clearAllAdminNotifications() {
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.CLEAR_ADMIN_NOTIFICATIONS_URL,
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
            error -> Log.e(TAG, "Error clearing admin notifications", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shop_id", String.valueOf(shopId));
                return params;
            }
        };
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead()) {
            markAdminNotificationRead(notification.getId());
            notification.setRead(true);
            if (unreadCount > 0) {
                unreadCount--;
                updateBadge(unreadCount);
            }
        }
        
        // Check if this is a stock notification (has productId)
        if (notification.getProductId() != null) {
            // Navigate to stock management page with product ID to open edit dialog
            Intent intent = new Intent(this, AdminStockManageActivity.class);
            intent.putExtra("edit_product_id", notification.getProductId().intValue());
            startActivity(intent);
        } else if (notification.getOrderId() != null) {
            // Navigate to order details
            Intent intent = new Intent(this, AdminOrderDetailsActivity.class);
            intent.putExtra("order_id", notification.getOrderId().intValue());
            startActivity(intent);
        }
    }
    
    private void markAdminNotificationRead(int notificationId) {
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.MARK_ADMIN_NOTIFICATION_READ_URL,
            response -> Log.d(TAG, "Admin notification marked as read"),
            error -> Log.e(TAG, "Error marking admin notification read", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shop_id", String.valueOf(shopId));
                params.put("notification_id", String.valueOf(notificationId));
                return params;
            }
        };
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadDashboardData() {
        int adminId = sessionManager.getUserId();
        if (adminId <= 0) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        String url = ApiConfig.GET_ADMIN_DASHBOARD_URL + "?admin_id=" + adminId;
        Log.d(TAG, "loadDashboardData: Loading from " + url);

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "loadDashboardData: Response = " + response);
                parseDashboardData(response);
            },
            error -> {
                showLoading(false);
                String errorMsg = "Network error";
                if (error != null) {
                    if (error.networkResponse != null) {
                        errorMsg = "Server error: " + error.networkResponse.statusCode;
                    } else if (error.getMessage() != null) {
                        errorMsg = error.getMessage();
                    } else if (error.getCause() != null) {
                        errorMsg = "Connection failed: " + error.getCause().getMessage();
                    } else {
                        errorMsg = "Cannot reach server - check WiFi/network";
                    }
                }
                Log.e(TAG, "loadDashboardData: " + errorMsg, error);
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseDashboardData(String response) {
        try {
            JSONObject json = new JSONObject(response);
            
            if (json.getBoolean("status")) {
                shopId = json.getInt("shop_id");
                String shopName = json.getString("shop_name");
                boolean isOnline = json.getInt("is_online") == 1;
                boolean deliveryAvailable = json.getInt("delivery_available") == 1;
                int todayOrders = json.getInt("today_orders");
                int pendingOrders = json.getInt("pending_orders");
                int completedOrders = json.getInt("completed_orders");
                int cancelledOrders = json.getInt("cancelled_orders");
                double todayEarnings = json.optDouble("today_earnings", 0);

                // Update UI
                tvStoreName.setText(shopName);
                tvTodayOrders.setText(String.valueOf(todayOrders));
                tvPendingOrders.setText(String.valueOf(pendingOrders));
                tvCompletedOrders.setText(String.valueOf(completedOrders));
                tvCancelledOrders.setText(String.valueOf(cancelledOrders));
                tvTodayEarnings.setText(String.format("₹%.2f\nToday's Earnings", todayEarnings));

                // Set toggle states without triggering listeners
                switchOnline.setOnCheckedChangeListener(null);
                switchDelivery.setOnCheckedChangeListener(null);
                
                switchOnline.setChecked(isOnline);
                switchDelivery.setChecked(deliveryAvailable);
                updateOnlineIndicator(isOnline);

                // Re-attach listeners
                switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (shopId > 0) {
                        updateShopStatus("is_online", isChecked ? 1 : 0);
                        updateOnlineIndicator(isChecked);
                    }
                });
                
                switchDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (shopId > 0) {
                        updateShopStatus("delivery_available", isChecked ? 1 : 0);
                    }
                });
                
                // Load admin notifications now that we have shop ID
                loadAdminNotifications();

                Log.d(TAG, "parseDashboardData: Shop loaded - " + shopName);
            } else {
                String message = json.optString("message", "Failed to load data");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseDashboardData: Parse error", e);
            Toast.makeText(this, "Error parsing dashboard data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOnlineIndicator(boolean isOnline) {
        if (isOnline) {
            tvOnlineStatus.setText(" Online");
            tvOnlineStatus.setTextColor(0xFF39FF14); // Green
            GradientDrawable dot = (GradientDrawable) viewOnlineDot.getBackground();
            dot.setColor(0xFF39FF14);
        } else {
            tvOnlineStatus.setText(" Offline");
            tvOnlineStatus.setTextColor(0xFFFF5252); // Red
            GradientDrawable dot = (GradientDrawable) viewOnlineDot.getBackground();
            dot.setColor(0xFFFF5252);
        }
    }

    private void updateShopStatus(String field, int value) {
        Log.d(TAG, "updateShopStatus: " + field + " = " + value);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_SHOP_STATUS_URL,
            response -> {
                Log.d(TAG, "updateShopStatus: Response = " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        String message = field.equals("is_online") 
                            ? (value == 1 ? "Shop is now Online" : "Shop is now Offline")
                            : (value == 1 ? "Delivery enabled" : "Delivery disabled");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, json.optString("message", "Update failed"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "updateShopStatus: Parse error", e);
                }
            },
            error -> {
                Log.e(TAG, "updateShopStatus: Error = " + error.getMessage());
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shop_id", String.valueOf(shopId));
                params.put("field", field);
                params.put("value", String.valueOf(value));
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        if (shopId > 0) {
            loadDashboardData();
        }
        // Start notification polling
        if (notificationHandler != null) {
            notificationHandler.post(notificationRunnable);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (notificationHandler != null) {
            notificationHandler.removeCallbacks(notificationRunnable);
        }
    }
}