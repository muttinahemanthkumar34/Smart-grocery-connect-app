package com.SIMATS.Groceryconnect.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.OrderHistoryAdapter;
import com.SIMATS.Groceryconnect.models.Order;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOrderHistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderActionListener {

    private static final String TAG = "UserOrderHistory";

    // Bottom Nav
    private LinearLayout navHome, navOrders, navAi, navCartText, navProfile;
    private EditText etSearchOrders;
    private RecyclerView rvOrdersHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private OrderHistoryAdapter orderAdapter;
    private List<Order> orderList;
    private RequestQueue requestQueue;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_history);

        // Get user ID from SessionManager
        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        initViews();
        setupRecyclerView();
        setupSearch();
        setupClickListeners();
        loadOrders();
    }

    private void initViews() {
        etSearchOrders = findViewById(R.id.et_search_orders);
        rvOrdersHistory = findViewById(R.id.rv_orders_history);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Bottom Nav
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navAi = findViewById(R.id.nav_ai);
        navCartText = findViewById(R.id.nav_cart_text);
        navProfile = findViewById(R.id.nav_profile);

        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderHistoryAdapter(orderList, this);
        rvOrdersHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrdersHistory.setAdapter(orderAdapter);
    }

    private void setupSearch() {
        etSearchOrders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                orderAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navOrders.setOnClickListener(v -> {
            // Already on orders
        });

        navAi.setOnClickListener(v -> {
            Toast.makeText(this, "AI Assistant coming soon!", Toast.LENGTH_SHORT).show();
        });

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
            Intent intent = new Intent(this, UserProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadOrders() {
        showLoading(true);

        String url = ApiConfig.GET_USER_ORDERS_URL + "?user_id=" + userId;
        Log.d(TAG, "loadOrders: Requesting " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                showLoading(false);
                Log.d(TAG, "loadOrders: Response = " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray ordersArray = json.getJSONArray("orders");
                        orderList.clear();

                        for (int i = 0; i < ordersArray.length(); i++) {
                            JSONObject orderJson = ordersArray.getJSONObject(i);
                            Order order = new Order();
                            order.setOrderId(orderJson.getInt("order_id"));
                            order.setShopId(orderJson.optInt("shop_id", 0));
                            order.setShopName(orderJson.getString("shop_name"));
                            order.setOrderType(orderJson.optString("order_type", "PICKUP"));
                            order.setTotalAmount(orderJson.getDouble("total_amount"));
                            order.setOrderStatus(orderJson.getString("order_status"));
                            order.setCreatedAt(orderJson.getString("created_at"));
                            order.setFeedbackSubmitted(orderJson.optInt("feedback_submitted", 0) == 1);
                            
                            Log.d(TAG, "Parsed order: id=" + order.getOrderId() + ", shopId=" + order.getShopId() + ", feedbackSubmitted=" + order.isFeedbackSubmitted());

                            orderList.add(order);
                        }

                        orderAdapter.updateOrders(orderList);
                        updateEmptyState();
                        Log.d(TAG, "loadOrders: Loaded " + orderList.size() + " orders");

                    } else {
                        Log.d(TAG, "loadOrders: No orders found");
                        updateEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "loadOrders: Error parsing", e);
                    Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "loadOrders: Network error", error);
                Toast.makeText(this, "Network error loading orders", Toast.LENGTH_SHORT).show();
                updateEmptyState();
            });

        requestQueue.add(request);
    }

    @Override
    public void onViewDetails(Order order) {
        // Navigate to User Order Details Activity
        Intent intent = new Intent(this, UserOrderDetailsActivity.class);
        intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_ID, order.getOrderId());
        intent.putExtra(UserOrderDetailsActivity.EXTRA_SHOP_NAME, order.getShopName());
        intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_STATUS, order.getOrderStatus());
        intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_TYPE, order.getOrderType());
        intent.putExtra(UserOrderDetailsActivity.EXTRA_TOTAL_AMOUNT, order.getTotalAmount());
        intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_DATE, order.getCreatedAt());
        startActivity(intent);
    }

    @Override
    public void onCancelOrder(Order order) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order from " + order.getShopName() + "?\n\nThis action cannot be undone.")
            .setPositiveButton("Yes, Cancel", (dialog, which) -> performCancelOrder(order))
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onSubmitFeedback(Order order) {
        showFeedbackDialog(order);
    }

    private void showFeedbackDialog(Order order) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_feedback);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.setCancelable(true);

        // Get dialog views
        RatingBar ratingShop = dialog.findViewById(R.id.rating_shop);
        RatingBar ratingProduct = dialog.findViewById(R.id.rating_product);
        RatingBar ratingPrice = dialog.findViewById(R.id.rating_price);
        EditText etComments = dialog.findViewById(R.id.et_comments);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_feedback);
        Button btnSubmit = dialog.findViewById(R.id.btn_submit_feedback);

        // Set default ratings
        ratingShop.setRating(5);
        ratingProduct.setRating(5);
        ratingPrice.setRating(5);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            float shopRating = ratingShop.getRating();
            float productRating = ratingProduct.getRating();
            float priceRating = ratingPrice.getRating();
            String comments = etComments.getText().toString().trim();

            // Calculate average rating
            float avgRating = (shopRating + productRating + priceRating) / 3.0f;
            int finalRating = Math.round(avgRating);
            
            // Ensure rating is between 1-5
            if (finalRating < 1) finalRating = 1;
            if (finalRating > 5) finalRating = 5;

            submitFeedback(order, finalRating, comments, dialog);
        });

        dialog.show();
    }

    private void submitFeedback(Order order, int rating, String comments, Dialog dialog) {
        showLoading(true);
        
        Log.d(TAG, "submitFeedback: orderId=" + order.getOrderId() + 
              ", shopId=" + order.getShopId() + 
              ", userId=" + userId + 
              ", rating=" + rating);

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.ADD_FEEDBACK_URL,
            response -> {
                showLoading(false);
                Log.d(TAG, "submitFeedback: Response = " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Refresh orders to update button state if needed
                        loadOrders();
                    } else {
                        String message = json.optString("message", "Failed to submit feedback");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "submitFeedback: Error parsing", e);
                    Toast.makeText(this, "Error submitting feedback", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "submitFeedback: Network error", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", String.valueOf(order.getOrderId()));
                params.put("user_id", String.valueOf(userId));
                params.put("shop_id", String.valueOf(order.getShopId()));
                params.put("rating", String.valueOf(rating));
                if (!comments.isEmpty()) {
                    params.put("comments", comments);
                }
                Log.d(TAG, "submitFeedback params: " + params.toString());
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void performCancelOrder(Order order) {
        showLoading(true);

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.CANCEL_ORDER_URL,
            response -> {
                showLoading(false);
                Log.d(TAG, "cancelOrder: Response = " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                        // Update the order status in adapter
                        orderAdapter.updateOrderStatus(order.getOrderId(), Order.STATUS_CANCELLED);
                    } else {
                        String message = json.optString("message", "Failed to cancel order");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "cancelOrder: Error parsing", e);
                    Toast.makeText(this, "Error cancelling order", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "cancelOrder: Network error", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", String.valueOf(order.getOrderId()));
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvOrdersHistory.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (orderAdapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrdersHistory.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrdersHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload orders when returning to this activity
        loadOrders();
    }
}
