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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * AdminShopSignupActivity handles the second step of admin registration.
 * Collects shop details and completes the admin + shop registration.
 */
public class AdminShopSignupActivity extends AppCompatActivity {

    private static final String TAG = "AdminShopSignup";

    // Existing fields
    private EditText etShopName, etAddress;
    private SwitchCompat switchDelivery;
    private Button btnSignup;
    private ProgressBar progressBar;
    private TextView tvBack;
    
    // New fields
    private EditText etCity, etPincode, etShopPhone;
    private EditText etOpeningTime, etClosingTime, etDeliveryRadius;
    private Spinner spinnerCategory;
    
    // Shop image fields
    private ImageView ivShopImage, ivCameraIcon;
    private String shopImageBase64 = null;
    
    // Category options
    private final String[] categories = {"Grocery", "Vegetables", "Fruits", "General Store", "Supermarket", "Pharmacy"};
    
    // Data from previous activity
    private String adminName, adminEmail, adminPhone, adminPassword;
    private String adminProfileImage = null; // Admin profile image from signup step 1
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_shop_signup);
        Log.d(TAG, "onCreate: AdminShopSignupActivity started");
        
        // Get data from intent
        adminName = getIntent().getStringExtra("name");
        adminEmail = getIntent().getStringExtra("email");
        adminPhone = getIntent().getStringExtra("phone");
        adminPassword = getIntent().getStringExtra("password");
        adminProfileImage = getIntent().getStringExtra("profile_image");
        
        Log.d(TAG, "onCreate: Received data - name=" + adminName + ", email=" + adminEmail);
        
        if (adminName == null || adminEmail == null || adminPhone == null || adminPassword == null) {
            Log.e(TAG, "onCreate: Missing registration data!");
            Toast.makeText(this, "Error: Missing registration data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupActivityLaunchers();
        initViews();
        setupSpinner();
        setupTimePickers();
        setupClickListeners();
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
                            shopImageBase64 = bitmapToBase64(photo);
                            ivShopImage.setImageBitmap(photo);
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
                            shopImageBase64 = bitmapToBase64(bitmap);
                            ivShopImage.setImageBitmap(bitmap);
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
        // Existing views
        etShopName = findViewById(R.id.etShopName);
        etAddress = findViewById(R.id.etAddress);
        switchDelivery = findViewById(R.id.switchDelivery);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        tvBack = findViewById(R.id.tvBack);
        
        // New views
        etCity = findViewById(R.id.etCity);
        etPincode = findViewById(R.id.etPincode);
        etShopPhone = findViewById(R.id.etShopPhone);
        etOpeningTime = findViewById(R.id.etOpeningTime);
        etClosingTime = findViewById(R.id.etClosingTime);
        etDeliveryRadius = findViewById(R.id.etDeliveryRadius);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        
        // Shop image views
        ivShopImage = findViewById(R.id.ivShopImage);
        ivCameraIcon = findViewById(R.id.ivCameraIcon);
        
        Log.d(TAG, "initViews: All views initialized");
    }
    
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
    
    private void setupTimePickers() {
        etOpeningTime.setOnClickListener(v -> showTimePicker(etOpeningTime));
        etClosingTime.setOnClickListener(v -> showTimePicker(etClosingTime));
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
            false // 12-hour format
        );
        timePickerDialog.show();
    }
    
    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> attemptAdminSignup());
        
        tvBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked, finishing activity");
            finish();
        });
        
        // Shop image picker
        View.OnClickListener imageClickListener = v -> showImagePickerDialog();
        ivShopImage.setOnClickListener(imageClickListener);
        ivCameraIcon.setOnClickListener(imageClickListener);
    }
    
    private void showImagePickerDialog() {
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
    
    private void attemptAdminSignup() {
        String shopName = etShopName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String shopPhone = etShopPhone.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String openingTime = etOpeningTime.getText().toString().trim();
        String closingTime = etClosingTime.getText().toString().trim();
        String deliveryRadius = etDeliveryRadius.getText().toString().trim();
        boolean isDeliveryAvailable = switchDelivery.isChecked();
        
        Log.d(TAG, "attemptAdminSignup: shopName=" + shopName + ", city=" + city + ", category=" + category);
        
        // Validate inputs
        if (shopName.isEmpty()) {
            etShopName.setError("Shop name is required");
            etShopName.requestFocus();
            return;
        }
        
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }
        
        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }
        
        if (pincode.isEmpty() || pincode.length() != 6) {
            etPincode.setError("Enter valid 6-digit pincode");
            etPincode.requestFocus();
            return;
        }
        
        if (shopPhone.isEmpty() || shopPhone.length() != 10) {
            etShopPhone.setError("Enter valid 10-digit phone");
            etShopPhone.requestFocus();
            return;
        }
        
        if (openingTime.isEmpty()) {
            etOpeningTime.setError("Opening time is required");
            etOpeningTime.requestFocus();
            return;
        }
        
        if (closingTime.isEmpty()) {
            etClosingTime.setError("Closing time is required");
            etClosingTime.requestFocus();
            return;
        }
        
        if (deliveryRadius.isEmpty()) {
            etDeliveryRadius.setError("Delivery radius is required");
            etDeliveryRadius.requestFocus();
            return;
        }
        
        showLoading(true);
        performAdminSignup(shopName, address, city, pincode, shopPhone, category, 
                          openingTime, closingTime, deliveryRadius, isDeliveryAvailable);
    }
    
    private void performAdminSignup(String shopName, String address, String city, 
                                    String pincode, String shopPhone, String category,
                                    String openingTime, String closingTime, 
                                    String deliveryRadius, boolean isDeliveryAvailable) {
        Log.d(TAG, "performAdminSignup: Making request to " + ApiConfig.REGISTER_ADMIN_URL);
        
        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            ApiConfig.REGISTER_ADMIN_URL,
            response -> {
                Log.d(TAG, "performAdminSignup: Response received: " + response);
                showLoading(false);
                handleSignupResponse(response);
            },
            error -> {
                showLoading(false);
                String errorMessage = "Network error. Please check your connection.";
                if (error.networkResponse != null) {
                    errorMessage = "Server error: " + error.networkResponse.statusCode;
                    Log.e(TAG, "performAdminSignup: Server error - Status: " + error.networkResponse.statusCode);
                    try {
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "performAdminSignup: Error response body: " + responseBody);
                    } catch (Exception e) {
                        Log.e(TAG, "performAdminSignup: Could not parse error response", e);
                    }
                } else {
                    Log.e(TAG, "performAdminSignup: Network error - " + error.getMessage(), error);
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Admin user data
                params.put("full_name", adminName);
                params.put("email", adminEmail);
                params.put("phone", adminPhone);
                params.put("password", adminPassword);
                // Shop data
                params.put("shop_name", shopName);
                params.put("shop_address", address);
                params.put("city", city);
                params.put("pincode", pincode);
                params.put("shop_phone", shopPhone);
                params.put("category", category);
                params.put("opening_time", openingTime);
                params.put("closing_time", closingTime);
                params.put("delivery_radius", deliveryRadius);
                params.put("delivery_available", isDeliveryAvailable ? "1" : "0");
                // Shop image (optional)
                if (shopImageBase64 != null) {
                    params.put("shop_image", shopImageBase64);
                }
                // Admin profile image (optional)
                if (adminProfileImage != null) {
                    params.put("profile_image", adminProfileImage);
                }
                
                Log.d(TAG, "performAdminSignup: Sending params - name=" + adminName + 
                      ", email=" + adminEmail + ", shopName=" + shopName + ", city=" + city);
                return params;
            }
        };
        
        // Set retry policy with 30 second timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    
    private void handleSignupResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean("status");
            String message = jsonResponse.getString("message");
            
            Log.d(TAG, "handleSignupResponse: success=" + success + ", message=" + message);
            
            if (success) {
                Log.d(TAG, "handleSignupResponse: Admin registration successful");
                Toast.makeText(this, "Admin registration successful! Please login.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminShopSignupActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.w(TAG, "handleSignupResponse: Admin registration failed - " + message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "handleSignupResponse: JSON parsing error", e);
            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignup.setEnabled(false);
            btnSignup.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSignup.setEnabled(true);
            btnSignup.setAlpha(1.0f);
        }
    }
}
