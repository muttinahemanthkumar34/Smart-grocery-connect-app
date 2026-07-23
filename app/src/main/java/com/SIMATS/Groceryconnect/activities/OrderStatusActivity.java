package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderStatusActivity extends AppCompatActivity {

    // Intent extra keys
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_ORDER_TYPE = "order_type";
    public static final String EXTRA_ORDER_NUMBER = "order_number";

    // Status constants (matching database values)
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_PREPARING = "PREPARING";
    private static final String STATUS_READY = "READY";
    private static final String STATUS_COMPLETED = "COMPLETED";

    // Views
    private ImageView ivBack;
    private TextView tvReadyTime, tvEstimatedTime, tvOrderNumber;
    private ImageView ivStep1Icon, ivStep2Icon, ivStep3Icon, ivStep4Icon;
    private TextView tvStep1Title, tvStep2Title, tvStep3Title, tvStep4Title;
    private TextView tvStep1Time, tvStep2Time, tvStep3Time;
    private View line1, line2, line3;
    private Button btnBackHome;

    // Data
    private int orderId;
    private String orderType;
    private String orderNumber;
    private boolean isDelivery;
    private RequestQueue requestQueue;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_status);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get intent data
        orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        orderType = getIntent().getStringExtra(EXTRA_ORDER_TYPE);
        orderNumber = getIntent().getStringExtra(EXTRA_ORDER_NUMBER);
        isDelivery = "DELIVERY".equalsIgnoreCase(orderType);

        requestQueue = Volley.newRequestQueue(this);
        refreshHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupClickListeners();
        setupDynamicLabels();
        fetchOrderStatus();
        startAutoRefresh();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvReadyTime = findViewById(R.id.tv_ready_time);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvOrderNumber = findViewById(R.id.tv_order_number);
        btnBackHome = findViewById(R.id.btn_back_home);

        // Step icons
        ivStep1Icon = findViewById(R.id.iv_step1_icon);
        ivStep2Icon = findViewById(R.id.iv_step2_icon);
        ivStep3Icon = findViewById(R.id.iv_step3_icon);
        ivStep4Icon = findViewById(R.id.iv_step4_icon);

        // Step titles
        tvStep1Title = findViewById(R.id.tv_step1_title);
        tvStep2Title = findViewById(R.id.tv_step2_title);
        tvStep3Title = findViewById(R.id.tv_step3_title);
        tvStep4Title = findViewById(R.id.tv_step4_title);

        // Step times
        tvStep1Time = findViewById(R.id.tv_step1_time);
        tvStep2Time = findViewById(R.id.tv_step2_time);
        tvStep3Time = findViewById(R.id.tv_step3_time);

        // Lines
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);

        // Set order number
        if (orderNumber != null && !orderNumber.isEmpty()) {
            tvOrderNumber.setText(orderNumber);
        }
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> navigateToHome());
        btnBackHome.setOnClickListener(v -> navigateToHome());
    }

    private void setupDynamicLabels() {
        // Update step 3 label based on order type
        if (isDelivery) {
            tvStep3Title.setText("Out for Delivery");
            tvEstimatedTime.setText("Estimated Delivery: Calculating...");
        } else {
            tvStep3Title.setText("Ready for Pickup");
            tvEstimatedTime.setText("Estimated Pickup: Calculating...");
        }
    }

    private void fetchOrderStatus() {
        if (orderId == -1) return;

        String url = ApiConfig.GET_ORDER_DETAILS_URL + "?order_id=" + orderId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONObject order = json.getJSONObject("order");
                        String status = order.optString("order_status", STATUS_PENDING);
                        updateStatusUI(status);
                    }
                } catch (Exception e) {
                    Log.e("OrderStatusActivity", "Error parsing order status", e);
                }
            },
            error -> {
                // Handle error silently, will retry on next refresh
            });

        requestQueue.add(request);
    }

    private void updateStatusUI(String status) {
        // Get current time for display
        String currentTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());

        // Reset all to incomplete state
        resetAllSteps();

        // Update based on status
        switch (status) {
            case STATUS_COMPLETED:
                markStepComplete(4, currentTime);
                line3.setBackgroundColor(0xFF39FF14);
                // Fall through
            case STATUS_READY:
                markStepComplete(3, currentTime);
                line2.setBackgroundColor(0xFF39FF14);
                tvReadyTime.setText("Order Ready!");
                updateEstimatedTime(isDelivery ? "Out for delivery" : "Ready for pickup");
                // Fall through
            case STATUS_PREPARING:
                markStepComplete(2, currentTime);
                line1.setBackgroundColor(0xFF39FF14);
                if (!status.equals(STATUS_READY) && !status.equals(STATUS_COMPLETED)) {
                    tvReadyTime.setText("Ready in 10 min");
                }
                // Fall through
            case STATUS_ACCEPTED:
                markStepComplete(1, currentTime);
                if (status.equals(STATUS_ACCEPTED)) {
                    tvReadyTime.setText("Ready in 30 min");
                }
                break;
            case STATUS_PENDING:
            default:
                tvReadyTime.setText("Waiting for confirmation");
                updateEstimatedTime("Order pending");
                break;
        }
    }

    private void resetAllSteps() {
        // Reset icons to gray
        ivStep1Icon.setBackgroundResource(R.drawable.bg_gray_circle);
        ivStep2Icon.setBackgroundResource(R.drawable.bg_gray_circle);
        ivStep3Icon.setBackgroundResource(R.drawable.bg_gray_circle);
        ivStep4Icon.setBackgroundResource(R.drawable.bg_gray_circle);

        // Reset titles to gray
        tvStep1Title.setTextColor(0xFF777777);
        tvStep2Title.setTextColor(0xFF777777);
        tvStep3Title.setTextColor(0xFF777777);
        tvStep4Title.setTextColor(0xFF777777);

        // Hide times
        tvStep1Time.setVisibility(View.GONE);
        tvStep2Time.setVisibility(View.GONE);
        tvStep3Time.setVisibility(View.GONE);

        // Reset lines to gray
        line1.setBackgroundColor(0xFF2A2A2A);
        line2.setBackgroundColor(0xFF2A2A2A);
        line3.setBackgroundColor(0xFF2A2A2A);
    }

    private void markStepComplete(int step, String time) {
        switch (step) {
            case 1:
                ivStep1Icon.setBackgroundResource(R.drawable.bg_green_circle);
                tvStep1Title.setTextColor(0xFFFFFFFF);
                tvStep1Time.setText(time);
                tvStep1Time.setVisibility(View.VISIBLE);
                break;
            case 2:
                ivStep2Icon.setBackgroundResource(R.drawable.bg_green_circle);
                tvStep2Title.setTextColor(0xFFFFFFFF);
                tvStep2Time.setText(time);
                tvStep2Time.setVisibility(View.VISIBLE);
                break;
            case 3:
                ivStep3Icon.setBackgroundResource(R.drawable.bg_green_circle);
                tvStep3Title.setTextColor(0xFFFFFFFF);
                tvStep3Time.setText(time);
                tvStep3Time.setVisibility(View.VISIBLE);
                break;
            case 4:
                ivStep4Icon.setBackgroundResource(R.drawable.bg_green_circle);
                tvStep4Title.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    private void updateEstimatedTime(String message) {
        String prefix = isDelivery ? "Estimated Delivery: " : "Estimated Pickup: ";
        tvEstimatedTime.setText(prefix + message);
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrderStatus();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, UserDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrderStatus();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }

    @Override
    public void onBackPressed() {
        navigateToHome();
    }
}
