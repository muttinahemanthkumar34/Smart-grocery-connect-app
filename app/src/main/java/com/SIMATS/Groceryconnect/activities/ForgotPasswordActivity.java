package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * ForgotPasswordActivity handles email input and sends OTP via backend.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";

    // Views
    private ImageView ivBack;
    private TextView tvTitle, tvBackToLogin;
    private EditText etEmail;
    private Button btnSendOtp;
    private ProgressBar progressBar;

    // Data
    private String role = "USER";

    // Colors
    private int accentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Get role from intent
        role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "USER";

        initViews();
        setupTheme();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        etEmail = findViewById(R.id.et_email);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupTheme() {
        if ("ADMIN".equalsIgnoreCase(role)) {
            // Orange theme for admin
            accentColor = getResources().getColor(R.color.accent_orange, getTheme());
            tvTitle.setTextColor(accentColor);
            tvBackToLogin.setTextColor(accentColor);
            btnSendOtp.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(accentColor));
        } else {
            // Green theme for user
            accentColor = getResources().getColor(R.color.accent_green, getTheme());
            tvTitle.setTextColor(accentColor);
            tvBackToLogin.setTextColor(accentColor);
            btnSendOtp.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(accentColor));
        }
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        tvBackToLogin.setOnClickListener(v -> finish());
        
        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            
            // Validate email
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }
            
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }
            
            // Send OTP via backend
            sendOtpViaEmail(email);
        });
    }

    private void sendOtpViaEmail(String email) {
        showLoading(true);
        Log.d(TAG, "Sending OTP to: " + email);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.SEND_EMAIL_OTP_URL,
            response -> {
                showLoading(false);
                Log.d(TAG, "Send OTP response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "OTP sent to your email!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to OTP verification screen
                        navigateToOtpVerification(email);
                    } else {
                        String message = json.optString("message", "Failed to send OTP");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Parse error", e);
                    Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Network error", error);
                Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("role", role);
                return params;
            }
        };

        // Set longer timeout for email sending (30 seconds)
        request.setRetryPolicy(new DefaultRetryPolicy(
            30000,  // 30 seconds timeout
            0,      // no retries
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("EMAIL", email);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendOtp.setEnabled(!show);
        btnSendOtp.setAlpha(show ? 0.6f : 1.0f);
    }
}
