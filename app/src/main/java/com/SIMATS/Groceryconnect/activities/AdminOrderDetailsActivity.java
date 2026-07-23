package com.SIMATS.Groceryconnect.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminOrderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AdminOrderDetails";

    // Views
    private ImageView ivBack;
    private TextView tvOrderId, tvStatus, tvOrderDate, tvOrderType;
    private TextView tvCustomerName, tvAddress;
    private LinearLayout layoutItems;
    private TextView tvItemsPlaceholder, tvTotalAmount;
    private ProgressBar progressBar;
    
    // Payment views
    private ImageView ivPaymentIcon;
    private TextView tvPaymentMethod, tvPaymentStatus, tvPaymentBadge;

    // Data
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getIntExtra("order_id", 0);
        if (orderId <= 0) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadOrderDetails();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvStatus = findViewById(R.id.tv_status);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderType = findViewById(R.id.tv_order_type);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvAddress = findViewById(R.id.tv_address);
        layoutItems = findViewById(R.id.layout_items);
        tvItemsPlaceholder = findViewById(R.id.tv_items_placeholder);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        progressBar = findViewById(R.id.progressBar);
        
        // Payment views
        ivPaymentIcon = findViewById(R.id.iv_payment_icon);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvPaymentStatus = findViewById(R.id.tv_payment_status);
        tvPaymentBadge = findViewById(R.id.tv_payment_badge);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadOrderDetails() {
        showLoading(true);
        String url = ApiConfig.GET_ORDER_DETAILS_URL + "?order_id=" + orderId;
        Log.d(TAG, "Loading order details from: " + url);

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "Response: " + response);
                parseOrderDetails(response);
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Error loading order details", error);
                Toast.makeText(this, "Failed to load order details", Toast.LENGTH_SHORT).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseOrderDetails(String response) {
        try {
            JSONObject json = new JSONObject(response);
            if (json.getBoolean("status")) {
                JSONObject order = json.getJSONObject("order");
                // Items are now inside the order object
                JSONArray items = order.optJSONArray("items");

                // Order info
                tvOrderId.setText("Order #GC-" + order.getInt("order_id"));
                
                String status = order.optString("order_status", "PLACED");
                tvStatus.setText(getStatusDisplayText(status));
                tvStatus.setTextColor(getStatusColor(status));

                String createdAt = order.optString("created_at", "");
                tvOrderDate.setText(formatDate(createdAt));

                String orderType = order.optString("order_type", "pickup");
                if ("delivery".equalsIgnoreCase(orderType)) {
                    tvOrderType.setText("🚚 Delivery Order");
                } else {
                    tvOrderType.setText("🏪 Pickup Order");
                }

                // Customer info
                String customerName = order.optString("user_name", "Customer");
                tvCustomerName.setText(customerName);

                String address = order.optString("delivery_address", "");
                if (address == null || address.isEmpty() || "null".equals(address)) {
                    address = "Pickup from store";
                }
                tvAddress.setText(address);

                // Total amount
                double totalAmount = order.optDouble("total_amount", 0);
                tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));

                // Payment method
                String paymentMethod = order.optString("payment_method", "COD");
                updatePaymentUI(paymentMethod, status);

                // Order items
                layoutItems.removeAllViews();
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        addItemView(
                            item.optString("product_name", "Item"),
                            item.optInt("quantity", 1),
                            item.optDouble("price", 0)
                        );
                    }
                }

            } else {
                Toast.makeText(this, json.optString("message", "Failed to load"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse error", e);
            Toast.makeText(this, "Error parsing order details", Toast.LENGTH_SHORT).show();
        }
    }

    private void addItemView(String productName, int quantity, double price) {
        LinearLayout itemRow = new LinearLayout(this);
        itemRow.setOrientation(LinearLayout.HORIZONTAL);
        itemRow.setGravity(Gravity.CENTER_VERTICAL);
        itemRow.setPadding(0, 8, 0, 8);

        // Quantity x Product
        TextView tvProduct = new TextView(this);
        tvProduct.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvProduct.setText(quantity + "x " + productName);
        tvProduct.setTextColor(0xFFFFFFFF);
        tvProduct.setTextSize(14);

        // Price
        TextView tvPrice = new TextView(this);
        tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", price * quantity));
        tvPrice.setTextColor(0xFFAAAAAA);
        tvPrice.setTextSize(14);

        itemRow.addView(tvProduct);
        itemRow.addView(tvPrice);
        layoutItems.addView(itemRow);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return date != null ? outputFormat.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getStatusDisplayText(String status) {
        if (status == null) return "Unknown";
        switch (status.toUpperCase()) {
            case "PLACED": return "Pending";
            case "PACKING": return "Packing";
            case "READY": return "Ready";
            case "OUT_FOR_DELIVERY": return "Out for Delivery";
            case "DELIVERED": return "Delivered";
            case "CANCELLED": return "Cancelled";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return 0xFFFF9800;
        switch (status.toUpperCase()) {
            case "DELIVERED": return 0xFF39FF14; // Green
            case "CANCELLED": return 0xFFFF4444; // Red
            default: return 0xFFFF9800; // Orange
        }
    }

    private void updatePaymentUI(String paymentMethod, String orderStatus) {
        boolean isUpi = "UPI".equalsIgnoreCase(paymentMethod);
        boolean isDelivered = "DELIVERED".equalsIgnoreCase(orderStatus);
        boolean isCancelled = "CANCELLED".equalsIgnoreCase(orderStatus);
        
        // Set icon and method text
        if (isUpi) {
            ivPaymentIcon.setImageResource(R.drawable.ic_upi);
            ivPaymentIcon.setColorFilter(0xFF6366F1); // Purple
            tvPaymentMethod.setText("UPI Payment");
            tvPaymentStatus.setText("Paid via UPI app");
            tvPaymentBadge.setText("PAID");
            tvPaymentBadge.setTextColor(0xFF10B981); // Green
        } else {
            ivPaymentIcon.setImageResource(R.drawable.ic_cash);
            ivPaymentIcon.setColorFilter(0xFF10B981); // Green
            tvPaymentMethod.setText("Cash On Delivery");
            
            if (isCancelled) {
                tvPaymentStatus.setText("Order cancelled");
                tvPaymentBadge.setText("CANCELLED");
                tvPaymentBadge.setTextColor(0xFFEF4444); // Red
            } else if (isDelivered) {
                tvPaymentStatus.setText("Payment collected");
                tvPaymentBadge.setText("PAID");
                tvPaymentBadge.setTextColor(0xFF10B981); // Green
            } else {
                tvPaymentStatus.setText("Collect on delivery");
                tvPaymentBadge.setText("UNPAID");
                tvPaymentBadge.setTextColor(0xFFF59E0B); // Orange/Amber
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
