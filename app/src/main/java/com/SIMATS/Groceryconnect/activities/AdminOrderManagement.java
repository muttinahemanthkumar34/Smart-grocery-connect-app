package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.AdminOrderAdapter;
import com.SIMATS.Groceryconnect.models.AdminOrder;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrderManagement extends AppCompatActivity implements AdminOrderAdapter.OnOrderActionListener {

    private static final String TAG = "AdminOrderManagement";

    // Views
    private TextView tabPending, tabCompleted;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;

    // Bottom Nav
    private LinearLayout navHome, navOrders, navProfile;

    // Data
    private SessionManager sessionManager;
    private AdminOrderAdapter adapter;
    private List<AdminOrder> allOrders = new ArrayList<>();
    private List<AdminOrder> pendingOrders = new ArrayList<>();
    private List<AdminOrder> completedOrders = new ArrayList<>();
    private int shopId = 0;
    private boolean showingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_order_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Check if should show completed orders tab (from intent)
        if (getIntent().getBooleanExtra("show_completed", false)) {
            showingCompleted = true;
            updateTabUI();
        }
        
        loadShopId();
    }

    private void initViews() {
        tabPending = findViewById(R.id.tab_pending);
        tabCompleted = findViewById(R.id.tab_completed);
        rvOrders = findViewById(R.id.rv_admin_orders);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        progressBar = findViewById(R.id.progressBar);

        // Bottom Nav
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(new ArrayList<>(), this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupClickListeners() {
        tabPending.setOnClickListener(v -> {
            if (showingCompleted) {
                showingCompleted = false;
                updateTabUI();
                displayOrders();
            }
        });

        tabCompleted.setOnClickListener(v -> {
            if (!showingCompleted) {
                showingCompleted = true;
                updateTabUI();
                displayOrders();
            }
        });

        // Bottom Nav
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrderManagement.this, AdminDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navOrders.setOnClickListener(v -> {
            // Already on orders
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrderManagement.this, AdminProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void updateTabUI() {
        if (showingCompleted) {
            tabPending.setBackgroundResource(android.R.color.transparent);
            tabPending.setTextColor(0xFFAAAAAA);
            tabCompleted.setBackgroundResource(R.drawable.bg_tab_selected);
            tabCompleted.setTextColor(0xFF000000);
        } else {
            tabPending.setBackgroundResource(R.drawable.bg_tab_selected);
            tabPending.setTextColor(0xFF000000);
            tabCompleted.setBackgroundResource(android.R.color.transparent);
            tabCompleted.setTextColor(0xFFAAAAAA);
        }
    }

    private void loadShopId() {
        int adminId = sessionManager.getUserId();
        if (adminId <= 0) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        String url = ApiConfig.GET_ADMIN_DASHBOARD_URL + "?admin_id=" + adminId;

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        shopId = json.getInt("shop_id");
                        loadOrders();
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Failed to get shop info", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    showLoading(false);
                    Log.e(TAG, "Parse error", e);
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Error loading shop", error);
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadOrders() {
        if (shopId <= 0) return;

        showLoading(true);
        String url = ApiConfig.GET_ADMIN_ORDERS_URL + "?shop_id=" + shopId;
        Log.d(TAG, "Loading orders from: " + url);

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "Orders response: " + response);
                parseOrders(response);
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Error loading orders", error);
                Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseOrders(String response) {
        allOrders.clear();
        pendingOrders.clear();
        completedOrders.clear();

        try {
            JSONObject json = new JSONObject(response);
            
            // Log the full response for debugging
            Log.d(TAG, "Full API response: " + response);
            
            if (json.getBoolean("status")) {
                JSONArray ordersArray = json.getJSONArray("orders");
                Log.d(TAG, "Found " + ordersArray.length() + " orders");

                for (int i = 0; i < ordersArray.length(); i++) {
                    JSONObject orderObj = ordersArray.getJSONObject(i);
                    AdminOrder order = new AdminOrder();
                    order.setOrderId(orderObj.getInt("order_id"));
                    order.setUserName(orderObj.optString("user_name", "Customer"));
                    order.setOrderType(orderObj.optString("order_type", "pickup"));
                    order.setTotalAmount(orderObj.optDouble("total_amount", 0));
                    order.setOrderStatus(orderObj.optString("order_status", "PLACED"));
                    order.setCreatedAt(orderObj.optString("created_at", ""));
                    order.setDeliveryAddress(orderObj.optString("delivery_address", ""));
                    order.setUserPhone(orderObj.optString("user_phone", ""));

                    Log.d(TAG, "Order #" + order.getOrderId() + " Status: " + order.getOrderStatus());

                    // Parse items if available
                    if (orderObj.has("items")) {
                        JSONArray itemsArray = orderObj.getJSONArray("items");
                        List<AdminOrder.OrderItem> items = new ArrayList<>();
                        for (int j = 0; j < itemsArray.length(); j++) {
                            JSONObject itemObj = itemsArray.getJSONObject(j);
                            AdminOrder.OrderItem item = new AdminOrder.OrderItem();
                            item.setProductName(itemObj.optString("product_name", "Item"));
                            item.setQuantity(itemObj.optInt("quantity", 1));
                            item.setPrice(itemObj.optDouble("price", 0));
                            items.add(item);
                        }
                        order.setItems(items);
                        Log.d(TAG, "Order #" + order.getOrderId() + " has " + items.size() + " items");
                    }

                    allOrders.add(order);

                    // Categorize orders
                    if (order.isCompleted()) {
                        completedOrders.add(order);
                    } else {
                        pendingOrders.add(order);
                    }
                }
                
                Log.d(TAG, "Pending: " + pendingOrders.size() + ", Completed: " + completedOrders.size());
            } else {
                String message = json.optString("message", "Unknown error");
                Log.e(TAG, "API returned false status: " + message);
                Toast.makeText(this, "API Error: " + message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse error: " + e.getMessage(), e);
            Toast.makeText(this, "Error parsing orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        displayOrders();
    }

    private void displayOrders() {
        List<AdminOrder> ordersToShow = showingCompleted ? completedOrders : pendingOrders;
        adapter.setShowingCompleted(showingCompleted);
        adapter.updateOrders(ordersToShow);

        if (ordersToShow.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
            tvEmptyMessage.setText(showingCompleted ? "No completed orders" : "No pending orders");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAcceptOrder(AdminOrder order) {
        updateOrderStatus(order, AdminOrder.STATUS_PACKING, "Order accepted and packing started");
    }

    @Override
    public void onDeclineOrder(AdminOrder order) {
        new AlertDialog.Builder(this)
            .setTitle("Decline Order")
            .setMessage("Are you sure you want to decline this order? This action cannot be undone.")
            .setPositiveButton("Decline", (dialog, which) -> {
                updateOrderStatus(order, AdminOrder.STATUS_CANCELLED, "Order declined");
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onUpdateStatus(AdminOrder order, String newStatus) {
        String message = "Status updated to " + getStatusDisplayText(newStatus);
        updateOrderStatus(order, newStatus, message);
    }

    @Override
    public void onViewDetails(AdminOrder order) {
        // Navigate to order details activity
        Intent intent = new Intent(this, AdminOrderDetailsActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    private void updateOrderStatus(AdminOrder order, String newStatus, String successMessage) {
        showLoading(true);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_ORDER_STATUS_URL,
            response -> {
                showLoading(false);
                Log.d(TAG, "Update status response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                        
                        // Update local data
                        order.setOrderStatus(newStatus);
                        
                        // Move order between lists if needed
                        if (AdminOrder.STATUS_CANCELLED.equalsIgnoreCase(newStatus) ||
                            AdminOrder.STATUS_DELIVERED.equalsIgnoreCase(newStatus)) {
                            pendingOrders.remove(order);
                            if (!completedOrders.contains(order)) {
                                completedOrders.add(0, order);
                            }
                        }
                        
                        // Refresh display
                        displayOrders();
                    } else {
                        Toast.makeText(this, json.optString("message", "Update failed"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Parse error", e);
                    Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Error updating status", error);
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", String.valueOf(order.getOrderId()));
                params.put("order_status", newStatus);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private String getStatusDisplayText(String status) {
        switch (status.toUpperCase()) {
            case "PLACED": return "Placed";
            case "PACKING": return "Packing";
            case "READY": return "Ready";
            case "OUT_FOR_DELIVERY": return "Out for Delivery";
            case "DELIVERED": return "Delivered";
            case "CANCELLED": return "Cancelled";
            default: return status;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shopId > 0) {
            loadOrders();
        }
    }
}