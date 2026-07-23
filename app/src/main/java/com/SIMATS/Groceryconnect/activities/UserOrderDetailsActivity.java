package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.OrderItemAdapter;
import com.SIMATS.Groceryconnect.models.OrderItem;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserOrderDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_ORDER_STATUS = "order_status";
    public static final String EXTRA_ORDER_TYPE = "order_type";
    public static final String EXTRA_TOTAL_AMOUNT = "total_amount";
    public static final String EXTRA_ORDER_DATE = "order_date";
    public static final String EXTRA_FROM_ORDER_PLACED = "from_order_placed";

    private ImageView ivBack;
    private TextView tvStatusIcon, tvStatusText, tvStatusMessage;
    private TextView tvOrderNumber, tvOrderDate, tvShopName, tvOrderType;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private LinearLayout layoutDeliveryFee;
    private RecyclerView rvOrderItems;
    private ProgressBar progressBar;
    private Button btnContactSupport;
    
    // Payment views
    private ImageView ivPaymentIcon;
    private TextView tvPaymentMethod, tvPaymentStatus, tvPaymentBadge;

    private int orderId;
    private String shopName, orderType, orderStatus;
    private double totalAmount;
    private String orderDate;
    private String shopPhone = "";
    private boolean fromOrderPlaced = false;
    private RequestQueue requestQueue;
    private List<OrderItem> orderItems;
    private OrderItemAdapter itemAdapter;
    private String paymentMethod = "COD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_details);

        getIntentData();
        initViews();
        setupRecyclerView();
        populateOrderInfo();
        loadOrderDetails();
    }

    private void getIntentData() {
        orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        orderStatus = getIntent().getStringExtra(EXTRA_ORDER_STATUS);
        orderType = getIntent().getStringExtra(EXTRA_ORDER_TYPE);
        totalAmount = getIntent().getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0);
        orderDate = getIntent().getStringExtra(EXTRA_ORDER_DATE);
        fromOrderPlaced = getIntent().getBooleanExtra(EXTRA_FROM_ORDER_PLACED, false);
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvStatusIcon = findViewById(R.id.tv_status_icon);
        tvStatusText = findViewById(R.id.tv_status_text);
        tvStatusMessage = findViewById(R.id.tv_status_message);
        tvOrderNumber = findViewById(R.id.tv_order_number);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvOrderType = findViewById(R.id.tv_order_type);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTotal = findViewById(R.id.tv_total);
        layoutDeliveryFee = findViewById(R.id.layout_delivery_fee);
        rvOrderItems = findViewById(R.id.rv_order_items);
        progressBar = findViewById(R.id.progressBar);
        btnContactSupport = findViewById(R.id.btn_contact_support);
        
        // Payment views
        ivPaymentIcon = findViewById(R.id.iv_payment_icon);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvPaymentStatus = findViewById(R.id.tv_payment_status);
        tvPaymentBadge = findViewById(R.id.tv_payment_badge);

        requestQueue = Volley.newRequestQueue(this);

        ivBack.setOnClickListener(v -> handleBackNavigation());

        btnContactSupport.setOnClickListener(v -> {
            if (shopPhone != null && !shopPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + shopPhone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Shop phone number not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        orderItems = new ArrayList<>();
        itemAdapter = new OrderItemAdapter(orderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setNestedScrollingEnabled(false);
        rvOrderItems.setAdapter(itemAdapter);
    }

    private void populateOrderInfo() {
        // Order number
        tvOrderNumber.setText("#ORD-" + orderId);

        // Order date
        if (orderDate != null) {
            tvOrderDate.setText(formatDate(orderDate));
        }

        // Shop name
        if (shopName != null) {
            tvShopName.setText(shopName);
        }

        // Order type
        if (orderType != null) {
            if (orderType.equalsIgnoreCase("DELIVERY")) {
                tvOrderType.setText("🚚 Delivery");
                layoutDeliveryFee.setVisibility(View.VISIBLE);
            } else {
                tvOrderType.setText("🛍️ Pickup");
                layoutDeliveryFee.setVisibility(View.GONE);
            }
        }

        // Total amount
        tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));

        // Update status UI
        updateStatusUI();
    }

    private void updateStatusUI() {
        if (orderStatus == null) {
            orderStatus = "PLACED";
        }

        switch (orderStatus.toUpperCase()) {
            case "PLACED":
                tvStatusIcon.setText("📦");
                tvStatusText.setText("Order Placed");
                tvStatusText.setTextColor(0xFFFF6B35); // Orange
                tvStatusMessage.setText("Your order has been placed and is waiting for confirmation");
                btnContactSupport.setVisibility(View.VISIBLE);
                break;

            case "PACKING":
                tvStatusIcon.setText("📋");
                tvStatusText.setText("Packing");
                tvStatusText.setTextColor(0xFFFF6B35); // Orange
                tvStatusMessage.setText("Your order is being packed and will be ready soon");
                btnContactSupport.setVisibility(View.VISIBLE);
                break;

            case "READY":
                tvStatusIcon.setText("✅");
                tvStatusText.setText("Ready");
                tvStatusText.setTextColor(0xFF00BFFF); // Blue
                if (orderType != null && orderType.equalsIgnoreCase("PICKUP")) {
                    tvStatusMessage.setText("Your order is ready for pickup!");
                } else {
                    tvStatusMessage.setText("Your order is ready and will be delivered soon");
                }
                btnContactSupport.setVisibility(View.VISIBLE);
                break;

            case "DELIVERED":
                tvStatusIcon.setText("🎉");
                tvStatusText.setText("Completed");
                tvStatusText.setTextColor(0xFF39FF14); // Green
                if (orderType != null && orderType.equalsIgnoreCase("PICKUP")) {
                    tvStatusMessage.setText("Order picked up successfully. Thank you for shopping!");
                } else {
                    tvStatusMessage.setText("Order delivered successfully. Thank you for shopping!");
                }
                btnContactSupport.setVisibility(View.GONE);
                break;

            case "CANCELLED":
                tvStatusIcon.setText("❌");
                tvStatusText.setText("Cancelled");
                tvStatusText.setTextColor(0xFFFF4444); // Red
                tvStatusMessage.setText("This order has been cancelled");
                btnContactSupport.setVisibility(View.GONE);
                break;

            default:
                tvStatusIcon.setText("📦");
                tvStatusText.setText(orderStatus);
                tvStatusText.setTextColor(0xFFFF6B35);
                tvStatusMessage.setText("Order in progress");
                btnContactSupport.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);

        String url = ApiConfig.GET_ORDER_DETAILS_URL + "?order_id=" + orderId;
        android.util.Log.d("UserOrderDetails", "Loading order from: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    android.util.Log.d("UserOrderDetails", "API Response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("status")) {
                            JSONObject orderData = json.getJSONObject("order");

                            // Get shop phone if available
                            shopPhone = orderData.optString("shop_phone", "");
                            android.util.Log.d("UserOrderDetails", "Shop phone: " + shopPhone);

                            // Parse order items
                            JSONArray itemsArray = orderData.optJSONArray("items");
                            android.util.Log.d("UserOrderDetails", "Items array: " + (itemsArray != null ? itemsArray.toString() : "null"));
                            
                            if (itemsArray != null && itemsArray.length() > 0) {
                                orderItems.clear();
                                double subtotal = 0;

                                for (int i = 0; i < itemsArray.length(); i++) {
                                    JSONObject itemJson = itemsArray.getJSONObject(i);
                                    OrderItem item = new OrderItem();
                                    item.setProductName(itemJson.optString("product_name", "Product"));
                                    item.setQuantity(itemJson.optInt("quantity", 1));
                                    item.setPrice(itemJson.optDouble("price", 0));
                                    item.setProductImage(itemJson.optString("product_image", ""));
                                    orderItems.add(item);

                                    subtotal += item.getPrice() * item.getQuantity();
                                    android.util.Log.d("UserOrderDetails", "Added item: " + item.getProductName() + " x" + item.getQuantity());
                                }

                                tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));

                                // Calculate delivery fee
                                if (orderType != null && orderType.equalsIgnoreCase("DELIVERY")) {
                                    double deliveryFee = totalAmount - subtotal;
                                    if (deliveryFee > 0) {
                                        tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryFee));
                                    } else {
                                        tvDeliveryFee.setText("Free");
                                    }
                                }

                                itemAdapter.notifyDataSetChanged();
                            } else {
                                android.util.Log.w("UserOrderDetails", "No items found in order");
                            }
                            
                            // Parse payment method
                            paymentMethod = orderData.optString("payment_method", "COD");
                            updatePaymentUI();
                        } else {
                            android.util.Log.e("UserOrderDetails", "API returned status false: " + json.optString("message"));
                        }
                    } catch (Exception e) {
                        android.util.Log.e("UserOrderDetails", "Error parsing response", e);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    android.util.Log.e("UserOrderDetails", "Network error", error);
                }
        );

        requestQueue.add(request);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            // Try date only format
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (Exception e2) {
                // Return as is
            }
        }
        return dateStr;
    }

    private void handleBackNavigation() {
        if (fromOrderPlaced) {
            // Navigate to dashboard when coming from order placement
            Intent intent = new Intent(this, UserDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Normal back navigation
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void updatePaymentUI() {
        boolean isUpi = "UPI".equalsIgnoreCase(paymentMethod);
        boolean isDelivered = orderStatus != null && orderStatus.equalsIgnoreCase("DELIVERED");
        boolean isCancelled = orderStatus != null && orderStatus.equalsIgnoreCase("CANCELLED");
        
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
                tvPaymentStatus.setText("Payment completed");
                tvPaymentBadge.setText("PAID");
                tvPaymentBadge.setTextColor(0xFF10B981); // Green
            } else {
                tvPaymentStatus.setText("Pay when you receive");
                tvPaymentBadge.setText("UNPAID");
                tvPaymentBadge.setTextColor(0xFFF59E0B); // Orange
            }
        }
    }
}
