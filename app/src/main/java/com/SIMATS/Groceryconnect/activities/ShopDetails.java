package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.bumptech.glide.Glide;

public class ShopDetails extends AppCompatActivity {

    // Intent extras keys
    public static final String EXTRA_SHOP_ID = "shop_id";
    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_SHOP_ADDRESS = "shop_address";
    public static final String EXTRA_SHOP_CITY = "shop_city";
    public static final String EXTRA_OPENING_TIME = "opening_time";
    public static final String EXTRA_CLOSING_TIME = "closing_time";
    public static final String EXTRA_DELIVERY_AVAILABLE = "delivery_available";
    public static final String EXTRA_SHOP_PHONE = "shop_phone";
    public static final String EXTRA_PINCODE = "pincode";
    public static final String EXTRA_SHOP_IMAGE = "shop_image";
    public static final String EXTRA_IS_ONLINE = "is_online";

    // Views
    private ImageView ivBack, ivShopImage;
    private TextView tvShopName, tvHours, tvAddress;
    private TextView tvDeliveryBadge, tvPickupBadge;
    private TextView tvMinOrder;
    private Button btnViewStock, btnViewFeedbacks;
    private View viewOnlineDot;

    // Shop data
    private int shopId;
    private String shopName, shopAddress, shopCity, shopPincode;
    private String openingTime, closingTime;
    private boolean deliveryAvailable;
    private String shopImage;
    private boolean isOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        // Get shop data from intent
        getIntentData();

        // Initialize views
        initViews();

        // Populate data
        populateShopData();

        // Set up click listeners
        setupClickListeners();
    }

    private void getIntentData() {
        shopId = getIntent().getIntExtra(EXTRA_SHOP_ID, -1);
        shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        shopAddress = getIntent().getStringExtra(EXTRA_SHOP_ADDRESS);
        shopCity = getIntent().getStringExtra(EXTRA_SHOP_CITY);
        openingTime = getIntent().getStringExtra(EXTRA_OPENING_TIME);
        closingTime = getIntent().getStringExtra(EXTRA_CLOSING_TIME);
        deliveryAvailable = getIntent().getBooleanExtra(EXTRA_DELIVERY_AVAILABLE, false);
        shopPincode = getIntent().getStringExtra(EXTRA_PINCODE);
        shopImage = getIntent().getStringExtra(EXTRA_SHOP_IMAGE);
        isOnline = getIntent().getBooleanExtra(EXTRA_IS_ONLINE, true);
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivShopImage = findViewById(R.id.iv_shop_image);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvHours = findViewById(R.id.tv_hours);
        tvAddress = findViewById(R.id.tv_address);
        tvDeliveryBadge = findViewById(R.id.tv_delivery_badge);
        tvPickupBadge = findViewById(R.id.tv_pickup_badge);
        tvMinOrder = findViewById(R.id.tv_min_order);
        btnViewStock = findViewById(R.id.btn_view_stock);
        btnViewFeedbacks = findViewById(R.id.btn_view_feedbacks);
        viewOnlineDot = findViewById(R.id.view_online_dot);
    }

    private void populateShopData() {
        // Set shop name
        if (shopName != null && !shopName.isEmpty()) {
            tvShopName.setText(shopName);
        }

        // Load shop image
        if (shopImage != null && !shopImage.isEmpty()) {
            String imageUrl = shopImage.startsWith("http") ? shopImage : ApiConfig.IMAGE_BASE_URL + shopImage;
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_shop_placeholder)
                    .error(R.drawable.img_shop_placeholder)
                    .centerCrop()
                    .into(ivShopImage);
        }

        // Set hours and online status
        if (isOnline) {
            if (openingTime != null && closingTime != null) {
                String hours = formatTime(openingTime) + " - " + formatTime(closingTime);
                tvHours.setText("Open " + hours);
            }
            // Green dot for online
            viewOnlineDot.setBackgroundResource(R.drawable.bg_green_dot);
        } else {
            tvHours.setText("Currently Closed");
            tvHours.setTextColor(0xFFFF6B6B); // Red color for closed
            // Red dot for offline
            viewOnlineDot.setBackgroundResource(R.drawable.bg_red_dot);
        }

        // Set address (shop_address, city, pincode)
        StringBuilder fullAddress = new StringBuilder();
        if (shopAddress != null && !shopAddress.isEmpty()) {
            fullAddress.append(shopAddress);
        }
        if (shopCity != null && !shopCity.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(shopCity);
        }
        if (shopPincode != null && !shopPincode.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" - ");
            fullAddress.append(shopPincode);
        }
        if (fullAddress.length() > 0) {
            tvAddress.setText(fullAddress.toString());
        }

        // Set delivery badge visibility
        if (deliveryAvailable) {
            tvDeliveryBadge.setVisibility(View.VISIBLE);
        } else {
            tvDeliveryBadge.setVisibility(View.GONE);
        }

        // Pickup is always available for now
        tvPickupBadge.setVisibility(View.VISIBLE);
        
        // Update button appearance if shop is closed
        if (!isOnline) {
            btnViewStock.setAlpha(0.5f);
            btnViewStock.setText("🏪  Shop Closed");
        }
    }

    private String formatTime(String time) {
        // Convert 24hr format (HH:MM:SS) to 12hr format
        try {
            if (time.contains(":")) {
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                
                String period = hour >= 12 ? "PM" : "AM";
                if (hour > 12) hour -= 12;
                if (hour == 0) hour = 12;
                
                return String.format("%d:%02d %s", hour, minute, period);
            }
        } catch (Exception e) {
            // Return original if parsing fails
        }
        return time;
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> finish());

        // View Shop Stock button
        btnViewStock.setOnClickListener(v -> {
            // Check if shop is online before allowing to view stock
            if (!isOnline) {
                Toast.makeText(this, "This shop is currently closed. Please try again later.", Toast.LENGTH_LONG).show();
                return;
            }
            
            Intent intent = new Intent(this, ShopStockActivity.class);
            intent.putExtra(ShopStockActivity.EXTRA_SHOP_ID, shopId);
            intent.putExtra(ShopStockActivity.EXTRA_SHOP_NAME, shopName);
            intent.putExtra(ShopStockActivity.EXTRA_SHOP_CITY, shopCity);
            intent.putExtra(ShopStockActivity.EXTRA_DELIVERY_AVAILABLE, deliveryAvailable);
            intent.putExtra(ShopStockActivity.EXTRA_SHOP_IMAGE, shopImage);
            startActivity(intent);
        });

        // View Feedbacks button
        btnViewFeedbacks.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserViewFeedbacksActivity.class);
            intent.putExtra("shop_id", shopId);
            intent.putExtra("shop_name", shopName);
            startActivity(intent);
        });
    }
}