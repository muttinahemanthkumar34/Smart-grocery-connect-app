package com.SIMATS.Groceryconnect.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.Map;

/**
 * SignupActivity handles user registration with User/Admin role toggle.
 * User: Single step signup
 * Admin: Two step signup (basic info, then shop details)
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnSignup;
    private RadioGroup rgRoleToggle;
    private RadioButton rbUser, rbAdmin;
    private TextView tvLogin, tvAppTitle;
    private ProgressBar progressBar;
    private ImageView ivProfilePic, ivCameraOverlay;
    private FrameLayout layoutProfilePic;
    
    private String selectedRole = "USER";
    private String profileImageBase64 = null;
    
    // Activity result launchers for camera/gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);
        Log.d(TAG, "onCreate: SignupActivity started");
        
        setupActivityLaunchers();
        initViews();
        setupToggleListener();
        setupClickListeners();
        
        // Initialize toggle visual state (User mode is default)
        updateUIForUserMode();
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
                            profileImageBase64 = bitmapToBase64(photo);
                            ivProfilePic.setImageBitmap(photo);
                            ivProfilePic.setImageTintList(null); // Remove tint to show actual image
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
                            profileImageBase64 = bitmapToBase64(bitmap);
                            ivProfilePic.setImageBitmap(bitmap);
                            ivProfilePic.setImageTintList(null); // Remove tint to show actual image
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
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        rgRoleToggle = findViewById(R.id.rgRoleToggle);
        rbUser = findViewById(R.id.rbUser);
        rbAdmin = findViewById(R.id.rbAdmin);
        tvLogin = findViewById(R.id.tvLogin);
        tvAppTitle = findViewById(R.id.tvAppTitle);
        progressBar = findViewById(R.id.progressBar);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivCameraOverlay = findViewById(R.id.ivCameraOverlay);
        layoutProfilePic = findViewById(R.id.layoutProfilePic);
        Log.d(TAG, "initViews: All views initialized");
    }
    
    private void setupToggleListener() {
        rgRoleToggle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbUser) {
                selectedRole = "USER";
                Log.d(TAG, "Role changed to: USER");
                updateUIForUserMode();
            } else if (checkedId == R.id.rbAdmin) {
                selectedRole = "ADMIN";
                Log.d(TAG, "Role changed to: ADMIN");
                updateUIForAdminMode();
            }
        });
    }
    
    private void updateUIForUserMode() {
        // Green accent for User
        tvAppTitle.setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
        tvLogin.setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
        btnSignup.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF10B981)); // Green
        btnSignup.setText("Sign Up");
        progressBar.setIndeterminateTintList(getResources().getColorStateList(R.color.accent_green, getTheme()));
        
        // Update toggle visual feedback
        rbUser.setBackgroundColor(0xFF10B981); // Green background
        rbUser.setTextColor(0xFFFFFFFF); // White text
        rbAdmin.setBackgroundColor(0xFF2A2A2A); // Dark background
        rbAdmin.setTextColor(0xFFAAAAAA); // Gray text
    }
    
    private void updateUIForAdminMode() {
        // Orange accent for Admin
        tvAppTitle.setTextColor(getResources().getColor(R.color.accent_orange, getTheme()));
        tvLogin.setTextColor(getResources().getColor(R.color.accent_orange, getTheme()));
        btnSignup.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF8C00)); // Orange
        btnSignup.setText("Next");
        progressBar.setIndeterminateTintList(getResources().getColorStateList(R.color.accent_orange, getTheme()));
        
        // Update toggle visual feedback
        rbAdmin.setBackgroundColor(0xFFFF8C00); // Orange background
        rbAdmin.setTextColor(0xFF000000); // Black text
        rbUser.setBackgroundColor(0xFF2A2A2A); // Dark background
        rbUser.setTextColor(0xFFAAAAAA); // Gray text
    }
    
    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> attemptSignup());
        
        tvLogin.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to LoginActivity");
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Profile picture click listener
        View.OnClickListener profilePicClickListener = v -> showImagePickerDialog();
        ivProfilePic.setOnClickListener(profilePicClickListener);
        ivCameraOverlay.setOnClickListener(profilePicClickListener);
        layoutProfilePic.setOnClickListener(profilePicClickListener);
    }
    
    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        new AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
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
    
    private void attemptSignup() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        Log.d(TAG, "attemptSignup: name=" + name + ", email=" + email + ", phone=" + phone + ", role=" + selectedRole);
        
        // Validate inputs
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            Log.w(TAG, "Signup validation failed: Name is empty");
            return;
        }
        
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            Log.w(TAG, "Signup validation failed: Phone is empty");
            return;
        }
        
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            Log.w(TAG, "Signup validation failed: Email is empty");
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            Log.w(TAG, "Signup validation failed: Invalid email format");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            Log.w(TAG, "Signup validation failed: Password is empty");
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            Log.w(TAG, "Signup validation failed: Password too short");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            Log.w(TAG, "Signup validation failed: Passwords do not match");
            return;
        }
        
        if (selectedRole.equals("USER")) {
            // Direct signup for User
            Log.d(TAG, "User role - performing direct signup");
            showLoading(true);
            performUserSignup(name, email, phone, password);
        } else {
            // Navigate to shop details for Admin
            Log.d(TAG, "Admin role - navigating to shop signup");
            navigateToShopSignup(name, email, phone, password);
        }
    }
    
    private void performUserSignup(String name, String email, String phone, String password) {
        Log.d(TAG, "performUserSignup: Making request to " + ApiConfig.REGISTER_USER_URL);
        
        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            ApiConfig.REGISTER_USER_URL,
            response -> {
                Log.d(TAG, "performUserSignup: Response received: " + response);
                showLoading(false);
                handleSignupResponse(response);
            },
            error -> {
                showLoading(false);
                String errorMessage = "Network error. Please check your connection.";
                String errorDetails = "";
                
                if (error.networkResponse != null) {
                    errorMessage = "Server error (HTTP " + error.networkResponse.statusCode + ")";
                    try {
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "performUserSignup: Error response body: " + responseBody);
                        JSONObject errorJson = new JSONObject(responseBody);
                        if (errorJson.has("message")) {
                            errorDetails = errorJson.getString("message");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "performUserSignup: Could not parse error response", e);
                        errorDetails = "Unable to parse server response";
                    }
                } else if (error.getMessage() != null) {
                    errorMessage = "Connection error: " + error.getMessage();
                    Log.e(TAG, "performUserSignup: Network error - " + error.getMessage(), error);
                } else {
                    Log.e(TAG, "performUserSignup: Unknown network error", error);
                }
                
                // Show detailed error message
                String finalMessage = errorMessage;
                if (!errorDetails.isEmpty()) {
                    finalMessage += "\n\nDetails: " + errorDetails;
                }
                Log.e(TAG, "performUserSignup: Final error message: " + finalMessage);
                Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("full_name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                // Add profile image if available
                if (profileImageBase64 != null) {
                    params.put("profile_image", profileImageBase64);
                }
                Log.d(TAG, "performUserSignup: Sending params - full_name=" + name + ", email=" + email + ", phone=" + phone);
                return params;
            }
        };
        
        // Set retry policy with 30 second timeout (30000 ms)
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
            30000, // 30 seconds timeout
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    
    private void handleSignupResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean status = jsonResponse.getBoolean("status");
            String message = jsonResponse.getString("message");
            
            Log.d(TAG, "handleSignupResponse: status=" + status + ", message=" + message);
            
            if (status) {
                Log.d(TAG, "handleSignupResponse: Registration successful");
                Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.w(TAG, "handleSignupResponse: Registration failed - " + message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "handleSignupResponse: JSON parsing error", e);
            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToShopSignup(String name, String email, String phone, String password) {
        Log.d(TAG, "navigateToShopSignup: Navigating to AdminShopSignupActivity");
        Intent intent = new Intent(SignupActivity.this, AdminShopSignupActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("password", password);
        // Pass profile image if available
        if (profileImageBase64 != null) {
            intent.putExtra("profile_image", profileImageBase64);
        }
        startActivity(intent);
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
