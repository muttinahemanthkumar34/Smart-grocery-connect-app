package com.SIMATS.Groceryconnect.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminProfile extends AppCompatActivity {

    private static final String TAG = "AdminProfile";

    // Views
    private TextView tvName, tvStoreName, tvPhone;
    private TextView tvShopAddress, tvShopCity;
    private Button btnEditProfile, btnEditShop, btnLogout;
    private LinearLayout rowViewFeedbacks, rowViewOrders;
    private SwitchCompat switchNotifications;
    private ProgressBar progressBar;

    // Bottom Nav
    private LinearLayout navHome, navOrders, navProfile;

    // Data
    private SessionManager sessionManager;
    private int shopId = 0;
    private String shopName = "";
    private String shopAddress = "";
    private String shopCity = "";
    private String shopPincode = "";
    private String shopPhone = "";
    private String openingTime = "";
    private String closingTime = "";
    private String deliveryRadius = "";
    private String shopImageUrl = "";
    private String shopUpiId = "";
    
    // Shop edit dialog image handling
    private String shopImageBase64 = null;
    private ImageView dialogShopImageView = null;
    
    // Admin profile image handling
    private ImageView ivAvatar, ivCameraOverlay;
    private FrameLayout layoutAvatar;
    private String profileImageBase64 = null;
    private boolean isProfileImageMode = false; // true for profile, false for shop
    
    // Activity result launchers for camera/gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        
        setupActivityLaunchers();
        initViews();
        setupClickListeners();
        loadProfileData();
        
        // Check if we should open edit shop dialog (coming from dashboard)
        if (getIntent().getBooleanExtra("open_edit_shop", false)) {
            // Wait for profile data to load, then open dialog
            findViewById(R.id.main).postDelayed(this::showEditShopDialog, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }
    
    private void setupActivityLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap photo = (Bitmap) extras.get("data");
                        if (photo != null) {
                            if (isProfileImageMode) {
                                profileImageBase64 = bitmapToBase64(photo);
                                ivAvatar.setImageBitmap(photo);
                                ivAvatar.setImageTintList(null);
                                uploadProfileImage();
                            } else {
                                shopImageBase64 = bitmapToBase64(photo);
                                if (dialogShopImageView != null) {
                                    dialogShopImageView.setImageBitmap(photo);
                                }
                            }
                        }
                    }
                }
            }
        );
        
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (isProfileImageMode) {
                                profileImageBase64 = bitmapToBase64(bitmap);
                                ivAvatar.setImageBitmap(bitmap);
                                ivAvatar.setImageTintList(null);
                                uploadProfileImage();
                            } else {
                                shopImageBase64 = bitmapToBase64(bitmap);
                                if (dialogShopImageView != null) {
                                    dialogShopImageView.setImageBitmap(bitmap);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading gallery image", e);
                        }
                    }
                }
            }
        );
        
        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Resize if too large
        int maxSize = 800;
        float scale = Math.min((float) maxSize / bitmap.getWidth(), (float) maxSize / bitmap.getHeight());
        if (scale < 1) {
            int newWidth = Math.round(bitmap.getWidth() * scale);
            int newHeight = Math.round(bitmap.getHeight() * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvStoreName = findViewById(R.id.tv_store_name);
        tvPhone = findViewById(R.id.tv_phone);
        tvShopAddress = findViewById(R.id.tv_shop_address);
        tvShopCity = findViewById(R.id.tv_shop_city);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnEditShop = findViewById(R.id.btn_edit_shop);
        btnLogout = findViewById(R.id.btn_logout);
        rowViewFeedbacks = findViewById(R.id.row_view_feedbacks);
        rowViewOrders = findViewById(R.id.row_view_orders);
        progressBar = findViewById(R.id.progressBar);
        ivAvatar = findViewById(R.id.iv_avatar);
        ivCameraOverlay = findViewById(R.id.iv_camera_overlay);
        layoutAvatar = findViewById(R.id.layout_avatar);

        // Bottom Nav
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navProfile = findViewById(R.id.nav_profile);
        
        // Notification toggle
        switchNotifications = findViewById(R.id.switch_notifications);
    }

    private void setupClickListeners() {
        // Edit Profile button - now shows personal details dialog
        btnEditProfile.setOnClickListener(v -> showEditPersonalDialog());

        // Edit Shop button - shows shop details dialog
        btnEditShop.setOnClickListener(v -> showEditShopDialog());

        // View Feedbacks
        rowViewFeedbacks.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminViewFeedbacksActivity.class);
            intent.putExtra(AdminViewFeedbacksActivity.EXTRA_SHOP_ID, shopId);
            startActivity(intent);
        });

        // View Incoming Orders
        rowViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminOrderManagement.class);
            intent.putExtra("show_pending", true);
            startActivity(intent);
        });

        // Logout button
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Bottom Nav
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfile.this, AdminDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfile.this, AdminOrderManagement.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            // Already on profile
        });
        
        // Notification toggle listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (shopId > 0) {
                updateAdminNotificationSetting(isChecked);
            }
        });
        
        // Profile avatar click listener
        View.OnClickListener avatarClickListener = v -> showProfileImagePickerDialog();
        ivAvatar.setOnClickListener(avatarClickListener);
        ivCameraOverlay.setOnClickListener(avatarClickListener);
        layoutAvatar.setOnClickListener(avatarClickListener);
    }
    
    private void showProfileImagePickerDialog() {
        isProfileImageMode = true;
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        new AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndOpen();
                } else if (which == 1) {
                    openGallery();
                }
            })
            .show();
    }
    
    private void uploadProfileImage() {
        if (profileImageBase64 == null) return;
        
        Toast.makeText(this, "Updating profile picture...", Toast.LENGTH_SHORT).show();
        
        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_ADMIN_PROFILE_URL,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.optBoolean("success", false)) {
                        String profileImage = json.optString("profile_image", "");
                        if (!profileImage.isEmpty()) {
                            sessionManager.setProfileImage(profileImage);
                        }
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            },
            error -> {
                Log.e(TAG, "Network error", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("admin_id", String.valueOf(sessionManager.getUserId()));
                params.put("name", sessionManager.getUserName());
                params.put("phone", sessionManager.getUserPhone());
                params.put("profile_image", profileImageBase64);
                return params;
            }
        };
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void showEditPersonalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_personal_profile, null);
        
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        ProgressBar dialogProgress = dialogView.findViewById(R.id.progressBar);
        
        // Pre-fill data
        etName.setText(sessionManager.getUserName());
        etPhone.setText(sessionManager.getUserPhone());
        etEmail.setText(sessionManager.getUserEmail());
        
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DarkDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            
            if (name.isEmpty()) {
                etName.setError("Name is required");
                return;
            }
            if (phone.isEmpty()) {
                etPhone.setError("Phone is required");
                return;
            }
            
            dialogProgress.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            
            // Make API call
            StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_ADMIN_PROFILE_URL,
                response -> {
                    dialogProgress.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optBoolean("success", false)) {
                            sessionManager.updateUserData(name, phone);
                            tvName.setText(name);
                            tvPhone.setText(phone);
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, json.optString("message", "Failed"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    dialogProgress.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("admin_id", String.valueOf(sessionManager.getUserId()));
                    params.put("name", name);
                    params.put("phone", phone);
                    return params;
                }
            };
            
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        });
        
        dialog.show();
        // Set dialog width to 90% of screen width
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setAttributes(params);
        }
    }
    
    private void showEditShopDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_shop_details, null);
        
        dialogShopImageView = dialogView.findViewById(R.id.iv_shop_image);
        ImageView ivCameraIcon = dialogView.findViewById(R.id.iv_camera_icon);
        EditText etShopName = dialogView.findViewById(R.id.et_shop_name);
        EditText etShopAddress = dialogView.findViewById(R.id.et_shop_address);
        EditText etCity = dialogView.findViewById(R.id.et_city);
        EditText etPincode = dialogView.findViewById(R.id.et_pincode);
        EditText etShopPhone = dialogView.findViewById(R.id.et_shop_phone);
        EditText etOpeningTime = dialogView.findViewById(R.id.et_opening_time);
        EditText etClosingTime = dialogView.findViewById(R.id.et_closing_time);
        EditText etDeliveryRadius = dialogView.findViewById(R.id.et_delivery_radius);
        EditText etUpiId = dialogView.findViewById(R.id.et_upi_id);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        ProgressBar dialogProgress = dialogView.findViewById(R.id.progressBar);
        
        // Reset image base64
        shopImageBase64 = null;
        
        // Pre-fill data
        etShopName.setText(shopName);
        etShopAddress.setText(shopAddress);
        etCity.setText(shopCity);
        etPincode.setText(shopPincode);
        etShopPhone.setText(shopPhone);
        etOpeningTime.setText(openingTime);
        etClosingTime.setText(closingTime);
        etDeliveryRadius.setText(deliveryRadius);
        etUpiId.setText(shopUpiId);
        
        // Load existing shop image
        if (shopImageUrl != null && !shopImageUrl.isEmpty()) {
            String fullUrl = shopImageUrl.startsWith("http") ? shopImageUrl : ApiConfig.IMAGE_BASE_URL + shopImageUrl;
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_shop_placeholder)
                .error(R.drawable.ic_shop_placeholder)
                .circleCrop()
                .into(dialogShopImageView);
        }
        
        // Time pickers
        etOpeningTime.setOnClickListener(v -> showTimePicker(etOpeningTime));
        etClosingTime.setOnClickListener(v -> showTimePicker(etClosingTime));
        
        // Image picker
        View.OnClickListener imageClickListener = v -> showImagePickerDialog();
        dialogShopImageView.setOnClickListener(imageClickListener);
        ivCameraIcon.setOnClickListener(imageClickListener);
        
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DarkDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String name = etShopName.getText().toString().trim();
            String address = etShopAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();
            String phone = etShopPhone.getText().toString().trim();
            String opening = etOpeningTime.getText().toString().trim();
            String closing = etClosingTime.getText().toString().trim();
            String radius = etDeliveryRadius.getText().toString().trim();
            String upiId = etUpiId.getText().toString().trim();
            
            if (name.isEmpty()) {
                etShopName.setError("Required");
                return;
            }
            
            dialogProgress.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            
            // Make API call
            StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.UPDATE_SHOP_URL,
                response -> {
                    dialogProgress.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optBoolean("success", false)) {
                            Toast.makeText(this, "Shop updated", Toast.LENGTH_SHORT).show();
                            loadProfileData(); // Refresh data
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, json.optString("message", "Failed"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    dialogProgress.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("shop_id", String.valueOf(shopId));
                    params.put("shop_name", name);
                    params.put("shop_address", address);
                    params.put("city", city);
                    params.put("pincode", pincode);
                    params.put("shop_phone", phone);
                    params.put("opening_time", opening);
                    params.put("closing_time", closing);
                    params.put("delivery_radius", radius);
                    params.put("upi_id", upiId);
                    if (shopImageBase64 != null) {
                        params.put("shop_image", shopImageBase64);
                    }
                    return params;
                }
            };
            
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        });
        
        dialog.show();
        // Set dialog width to 90% of screen width
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setAttributes(params);
        }
    }
    
    private void showTimePicker(EditText targetField) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minuteOfHour) -> {
                String amPm = hourOfDay >= 12 ? "PM" : "AM";
                int displayHour = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minuteOfHour, amPm);
                targetField.setText(time);
            },
            hour,
            minute,
            false
        );
        timePickerDialog.show();
    }
    
    private void showImagePickerDialog() {
        isProfileImageMode = false; // Shop image mode
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        new AlertDialog.Builder(this)
            .setTitle("Select Shop Image")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndOpen();
                } else if (which == 1) {
                    openGallery();
                }
            })
            .show();
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void loadProfileData() {
        int adminId = sessionManager.getUserId();
        if (adminId <= 0) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            performLogout();
            return;
        }

        showLoading(true);
        String url = ApiConfig.GET_ADMIN_DASHBOARD_URL + "?admin_id=" + adminId;
        Log.d(TAG, "loadProfileData: Loading from " + url);

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "loadProfileData: Response = " + response);
                parseProfileData(response);
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "loadProfileData: Error = " + error.getMessage());
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                loadFromSession();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseProfileData(String response) {
        try {
            JSONObject json = new JSONObject(response);

            if (json.getBoolean("status")) {
                shopId = json.getInt("shop_id");
                shopName = json.getString("shop_name");
                shopAddress = json.optString("shop_address", "Address not set");
                shopCity = json.optString("city", "City not set");
                shopPincode = json.optString("pincode", "");
                shopPhone = json.optString("shop_phone", "");
                openingTime = json.optString("opening_time", "");
                closingTime = json.optString("closing_time", "");
                deliveryRadius = json.optString("delivery_radius", "");
                shopImageUrl = json.optString("shop_image", "");
                shopUpiId = json.optString("upi_id", "");

                // Get admin details from session
                String adminName = sessionManager.getUserName();
                String adminPhone = sessionManager.getUserPhone();

                // Update UI
                tvName.setText(adminName != null && !adminName.isEmpty() ? adminName : "Admin");
                tvStoreName.setText(shopName);
                tvPhone.setText(adminPhone != null && !adminPhone.isEmpty() ? adminPhone : "Phone not set");
                tvShopAddress.setText(shopAddress);
                tvShopCity.setText(shopCity);

                Log.d(TAG, "parseProfileData: Profile loaded - " + adminName + ", Shop: " + shopName);
                
                // Load profile image
                String profileImage = sessionManager.getProfileImage();
                if (profileImage != null && !profileImage.isEmpty()) {
                    String imageUrl = profileImage.startsWith("http") ? profileImage : ApiConfig.IMAGE_BASE_URL + profileImage;
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(ivAvatar);
                    ivAvatar.setImageTintList(null);
                }
                
                // Load notification setting
                loadAdminNotificationSetting();
            } else {
                String message = json.optString("message", "Failed to load data");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                loadFromSession();
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseProfileData: Parse error", e);
            Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
            loadFromSession();
        }
    }

    private void loadFromSession() {
        String adminName = sessionManager.getUserName();
        String adminPhone = sessionManager.getUserPhone();

        tvName.setText(adminName != null && !adminName.isEmpty() ? adminName : "Admin");
        tvPhone.setText(adminPhone != null && !adminPhone.isEmpty() ? adminPhone : "Phone not set");
        tvStoreName.setText("Store");
        tvShopAddress.setText("Address not available");
        tvShopCity.setText("City not available");
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> performLogout())
            .setNegativeButton("No", null)
            .show();
    }

    private void performLogout() {
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void loadAdminNotificationSetting() {
        if (shopId <= 0) return;
        
        String url = ApiConfig.GET_ADMIN_NOTIFICATION_SETTING_URL + "?shop_id=" + shopId;
        
        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        boolean enabled = json.getBoolean("notifications_enabled");
                        // Set without triggering listener
                        switchNotifications.setOnCheckedChangeListener(null);
                        switchNotifications.setChecked(enabled);
                        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (shopId > 0) {
                                updateAdminNotificationSetting(isChecked);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing notification setting", e);
                }
            },
            error -> Log.e(TAG, "Error loading notification setting", error)
        );
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void updateAdminNotificationSetting(boolean enabled) {
        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_ADMIN_NOTIFICATION_SETTING_URL,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, 
                            enabled ? "Notifications enabled" : "Notifications disabled", 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing update response", e);
                }
            },
            error -> {
                Log.e(TAG, "Error updating notification setting", error);
                // Revert switch on error
                switchNotifications.setOnCheckedChangeListener(null);
                switchNotifications.setChecked(!enabled);
                switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (shopId > 0) {
                        updateAdminNotificationSetting(isChecked);
                    }
                });
                Toast.makeText(this, "Failed to update setting", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shop_id", String.valueOf(shopId));
                params.put("notifications_enabled", enabled ? "1" : "0");
                return params;
            }
        };
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}