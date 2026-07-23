package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SIMATS.Groceryconnect.R;

public class OrderPlaced extends AppCompatActivity {

    // Intent extra keys
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_ORDER_NUMBER = "order_number";
    public static final String EXTRA_STORE_NAME = "store_name";
    public static final String EXTRA_ORDER_TYPE = "order_type";
    public static final String EXTRA_TOTAL_AMOUNT = "total_amount";

    // Views
    private ImageView ivSuccessIcon;
    private TextView tvSubtitle;
    private TextView tvOrderNumber;
    private TextView tvStoreName;
    private TextView tvEstimatedLabel;
    private TextView tvEstimatedTime;
    private TextView tvOrderType;
    private TextView tvTotalAmount;
    private Button btnViewOrderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_placed);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadOrderData();
        playCheckAnimation();
        setupClickListeners();
    }

    private void initViews() {
        ivSuccessIcon = findViewById(R.id.iv_success_icon);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvOrderNumber = findViewById(R.id.tv_order_number);
        tvStoreName = findViewById(R.id.tv_store_name);
        tvEstimatedLabel = findViewById(R.id.tv_estimated_label);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvOrderType = findViewById(R.id.tv_order_type);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnViewOrderStatus = findViewById(R.id.btn_view_order_status);
    }

    // Data for passing to OrderStatus
    private int orderId;
    private String orderNumber;
    private String orderType;

    private void loadOrderData() {
        Intent intent = getIntent();
        
        // Get order data from intent
        orderId = intent.getIntExtra(EXTRA_ORDER_ID, -1);
        orderNumber = intent.getStringExtra(EXTRA_ORDER_NUMBER);
        String storeName = intent.getStringExtra(EXTRA_STORE_NAME);
        orderType = intent.getStringExtra(EXTRA_ORDER_TYPE);
        double totalAmount = intent.getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0);

        // Set order number
        if (orderNumber != null && !orderNumber.isEmpty()) {
            tvOrderNumber.setText(orderNumber);
        } else {
            // Generate a random order number if not provided
            int randomNum = 1000 + (int)(Math.random() * 9000);
            tvOrderNumber.setText("#GRC-" + randomNum);
        }

        // Set store name
        if (storeName != null && !storeName.isEmpty()) {
            tvStoreName.setText(storeName);
        }

        // Set order type and update labels accordingly
        boolean isDelivery = "DELIVERY".equalsIgnoreCase(orderType);
        tvOrderType.setText(isDelivery ? "Delivery" : "Pickup");
        tvEstimatedLabel.setText(isDelivery ? "Estimated Delivery" : "Estimated Pickup");
        tvSubtitle.setText(isDelivery ? 
            "Your items are being prepared for delivery." : 
            "Your items are being prepared for pickup.");

        // Set estimated time (fixed at 30-45 minutes)
        tvEstimatedTime.setText("30-45 minutes");

        // Set total amount
        tvTotalAmount.setText(String.format("₹%.2f", totalAmount));
    }

    private void playCheckAnimation() {
        // Load and start the check animation
        Animation checkAnim = AnimationUtils.loadAnimation(this, R.anim.check_animation);
        ivSuccessIcon.startAnimation(checkAnim);
    }

    private void setupClickListeners() {
        btnViewOrderStatus.setOnClickListener(v -> {
            // Navigate to User Order Details screen
            Intent intent = new Intent(this, UserOrderDetailsActivity.class);
            intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_ID, orderId);
            intent.putExtra(UserOrderDetailsActivity.EXTRA_SHOP_NAME, getIntent().getStringExtra(EXTRA_STORE_NAME));
            intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_STATUS, "PLACED");
            intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_TYPE, orderType);
            intent.putExtra(UserOrderDetailsActivity.EXTRA_TOTAL_AMOUNT, getIntent().getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0));
            intent.putExtra(UserOrderDetailsActivity.EXTRA_ORDER_DATE, "");
            intent.putExtra(UserOrderDetailsActivity.EXTRA_FROM_ORDER_PLACED, true);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Override back press to go to dashboard instead of cart
        Intent intent = new Intent(this, UserDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}