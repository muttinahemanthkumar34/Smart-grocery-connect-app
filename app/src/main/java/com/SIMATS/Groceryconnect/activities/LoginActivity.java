package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.FCMHelper;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginActivity handles user authentication with User/Admin role toggle.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private RadioGroup rgRoleToggle;
    private RadioButton rbUser, rbAdmin;
    private TextView tvSignUp, tvForgotPassword, tvAppTitle;
    private ProgressBar progressBar;
    
    private SessionManager sessionManager;
    private String selectedRole = "USER"; // Default role
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        Log.d(TAG, "onCreate: LoginActivity started");
        
        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in, navigating based on role");
            navigateToHome(sessionManager.getUserRole());
            return;
        }
        
        // Initialize views
        initViews();
        
        // Setup toggle listener
        setupToggleListener();
        
        // Setup click listeners
        setupClickListeners();
        
        // Initialize toggle visual state (User mode is default)
        updateUIForUserMode();
        
        // Request notification permission (Android 13+)
        FCMHelper.requestNotificationPermission(this);
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        rgRoleToggle = findViewById(R.id.rgRoleToggle);
        rbUser = findViewById(R.id.rbUser);
        rbAdmin = findViewById(R.id.rbAdmin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvAppTitle = findViewById(R.id.tvAppTitle);
        progressBar = findViewById(R.id.progressBar);
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
        // Change accent color to green for User mode
        tvAppTitle.setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
        tvSignUp.setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
        tvForgotPassword.setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
        btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF10B981)); // Green
        progressBar.setIndeterminateTintList(getResources().getColorStateList(R.color.accent_green, getTheme()));
        
        // Update toggle visual feedback
        rbUser.setBackgroundColor(0xFF10B981); // Green background
        rbUser.setTextColor(0xFFFFFFFF); // White text
        rbAdmin.setBackgroundColor(0xFF2A2A2A); // Dark background
        rbAdmin.setTextColor(0xFFAAAAAA); // Gray text
    }
    
    private void updateUIForAdminMode() {
        // Change accent color to orange for Admin mode
        tvAppTitle.setTextColor(getResources().getColor(R.color.accent_orange, getTheme()));
        tvSignUp.setTextColor(getResources().getColor(R.color.accent_orange, getTheme()));
        tvForgotPassword.setTextColor(getResources().getColor(R.color.accent_orange, getTheme()));
        btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF8C00)); // Orange
        progressBar.setIndeterminateTintList(getResources().getColorStateList(R.color.accent_orange, getTheme()));
        
        // Update toggle visual feedback
        rbAdmin.setBackgroundColor(0xFFFF8C00); // Orange background
        rbAdmin.setTextColor(0xFF000000); // Black text
        rbUser.setBackgroundColor(0xFF2A2A2A); // Dark background
        rbUser.setTextColor(0xFFAAAAAA); // Gray text
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        
        tvSignUp.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to SignupActivity");
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("ROLE", selectedRole);
            startActivity(intent);
        });
    }
    
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        Log.d(TAG, "attemptLogin: email=" + email + ", role=" + selectedRole);
        
        // Validate inputs
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            Log.w(TAG, "Login validation failed: Email is empty");
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            Log.w(TAG, "Login validation failed: Invalid email format");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            Log.w(TAG, "Login validation failed: Password is empty");
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            Log.w(TAG, "Login validation failed: Password too short");
            return;
        }
        
        // Show progress and disable button
        showLoading(true);
        
        // Make login request
        performLogin(email, password, selectedRole);
    }
    
    private void performLogin(String email, String password, String role) {
        Log.d(TAG, "performLogin: Making request to " + ApiConfig.LOGIN_URL);
        
        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            ApiConfig.LOGIN_URL,
            response -> {
                Log.d(TAG, "performLogin: Response received: " + response);
                showLoading(false);
                handleLoginResponse(response);
            },
            error -> {
                showLoading(false);
                String errorMessage = "Network error. Please check your connection.";
                if (error.networkResponse != null) {
                    errorMessage = "Server error: " + error.networkResponse.statusCode;
                    Log.e(TAG, "performLogin: Server error - Status: " + error.networkResponse.statusCode);
                    try {
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "performLogin: Error response body: " + responseBody);
                    } catch (Exception e) {
                        Log.e(TAG, "performLogin: Could not parse error response", e);
                    }
                } else {
                    Log.e(TAG, "performLogin: Network error - " + error.getMessage(), error);
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("role", role);
                Log.d(TAG, "performLogin: Sending params - email=" + email + ", role=" + role);
                return params;
            }
        };
        
        // Add request to queue
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    
    private void handleLoginResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean("status"); // PHP returns "status" not "success"
            String message = jsonResponse.getString("message");
            
            Log.d(TAG, "handleLoginResponse: success=" + success + ", message=" + message);
            
            if (success) {
                // Parse user data from root level (PHP returns flat structure)
                int userId = jsonResponse.getInt("user_id");
                String role = jsonResponse.getString("role");
                String name = jsonResponse.optString("full_name", "User");
                String phone = jsonResponse.optString("phone", "");
                String email = jsonResponse.optString("email", etEmail.getText().toString().trim());
                String profileImage = jsonResponse.optString("profile_image", "");
                
                Log.d(TAG, "handleLoginResponse: User logged in - id=" + userId + ", name=" + name + ", role=" + role);
                
                // Save session with profile image
                sessionManager.createLoginSession(userId, name, email, role, phone, profileImage);
                
                // For admin users, also save shop_id if present
                if (jsonResponse.has("shop_id")) {
                    int shopId = jsonResponse.getInt("shop_id");
                    sessionManager.setShopId(shopId);
                    Log.d(TAG, "handleLoginResponse: Admin shop_id=" + shopId);
                }
                
                // Register FCM token for push notifications
                FCMHelper.registerToken(this);
                
                // Show success message
                Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                
                // Navigate based on role
                navigateToHome(role);
            } else {
                // Show error message
                Log.w(TAG, "handleLoginResponse: Login failed - " + message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "handleLoginResponse: JSON parsing error", e);
            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToHome(String role) {
        Intent intent;
        if ("ADMIN".equalsIgnoreCase(role)) {
            Log.d(TAG, "navigateToHome: Navigating to AdminDashboard");
            intent = new Intent(LoginActivity.this, AdminDashboard.class);
        } else {
            Log.d(TAG, "navigateToHome: Navigating to UserDashboard");
            intent = new Intent(LoginActivity.this, UserDashboard.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);
        }
    }
}
