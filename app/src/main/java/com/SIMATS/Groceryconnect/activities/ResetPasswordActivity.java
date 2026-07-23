package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * ResetPasswordActivity handles new password input after OTP verification.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPassword";

    // Views
    private ImageView ivBack, ivTogglePassword, ivToggleConfirmPassword, ivSuccessCheck;
    private TextView tvTitle, tvPasswordStrength;
    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private LinearLayout layoutSuccess;
    private View strengthBar1, strengthBar2, strengthBar3, strengthBar4;

    // Data
    private String email;
    private String role = "USER";
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Colors
    private int accentColor;
    private int weakColor = 0xFFFF4444;
    private int mediumColor = 0xFFFFAA00;
    private int strongColor = 0xFF00CC00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get data from intent
        email = getIntent().getStringExtra("EMAIL");
        role = getIntent().getStringExtra("ROLE");
        boolean verified = getIntent().getBooleanExtra("VERIFIED", false);
        
        if (role == null) role = "USER";

        // Security check - must come from OTP verification
        if (!verified) {
            Toast.makeText(this, "Invalid access", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupTheme();
        setupPasswordStrengthIndicator();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvPasswordStrength = findViewById(R.id.tv_password_strength);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);
        layoutSuccess = findViewById(R.id.layout_success);
        ivSuccessCheck = findViewById(R.id.iv_success_check);

        strengthBar1 = findViewById(R.id.strength_bar_1);
        strengthBar2 = findViewById(R.id.strength_bar_2);
        strengthBar3 = findViewById(R.id.strength_bar_3);
        strengthBar4 = findViewById(R.id.strength_bar_4);
    }

    private void setupTheme() {
        if ("ADMIN".equalsIgnoreCase(role)) {
            // Orange theme for admin
            accentColor = getResources().getColor(R.color.accent_orange, getTheme());
        } else {
            // Green theme for user
            accentColor = getResources().getColor(R.color.accent_green, getTheme());
        }
        
        tvTitle.setTextColor(accentColor);
        btnResetPassword.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(accentColor));
        ivSuccessCheck.setImageTintList(ColorStateList.valueOf(accentColor));
    }

    private void setupPasswordStrengthIndicator() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        int defaultColor = getResources().getColor(R.color.input_border, getTheme());

        // Reset all bars
        strengthBar1.setBackgroundColor(defaultColor);
        strengthBar2.setBackgroundColor(defaultColor);
        strengthBar3.setBackgroundColor(defaultColor);
        strengthBar4.setBackgroundColor(defaultColor);

        if (password.isEmpty()) {
            tvPasswordStrength.setText("");
            return;
        }

        if (strength >= 1) strengthBar1.setBackgroundColor(weakColor);
        if (strength >= 2) {
            strengthBar1.setBackgroundColor(mediumColor);
            strengthBar2.setBackgroundColor(mediumColor);
        }
        if (strength >= 3) {
            strengthBar1.setBackgroundColor(strongColor);
            strengthBar2.setBackgroundColor(strongColor);
            strengthBar3.setBackgroundColor(strongColor);
        }
        if (strength >= 4) {
            strengthBar4.setBackgroundColor(strongColor);
        }

        // Update text
        if (strength <= 1) {
            tvPasswordStrength.setText("Weak password");
            tvPasswordStrength.setTextColor(weakColor);
        } else if (strength == 2) {
            tvPasswordStrength.setText("Medium password");
            tvPasswordStrength.setTextColor(mediumColor);
        } else if (strength == 3) {
            tvPasswordStrength.setText("Strong password");
            tvPasswordStrength.setTextColor(strongColor);
        } else {
            tvPasswordStrength.setText("Very strong password");
            tvPasswordStrength.setTextColor(strongColor);
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        
        if (password.length() >= 6) strength++;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;
        
        return Math.min(strength, 4);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etNewPassword, isPasswordVisible);
        });

        ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible);
        });

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void togglePasswordVisibility(EditText editText, boolean visible) {
        if (visible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        editText.setSelection(editText.getText().length());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation
        if (newPassword.isEmpty()) {
            etNewPassword.setError("Password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Send reset request
        showLoading(true);
        
        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.RESET_PASSWORD_URL,
            response -> {
                Log.d(TAG, "Reset password response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        showSuccessAnimation();
                    } else {
                        showLoading(false);
                        String message = json.optString("message", "Failed to reset password");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    showLoading(false);
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
                params.put("new_password", newPassword);
                params.put("role", role);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showSuccessAnimation() {
        showLoading(false);
        
        // Hide password form, show success
        findViewById(R.id.iv_back).setVisibility(View.GONE);
        btnResetPassword.setVisibility(View.GONE);
        
        layoutSuccess.setVisibility(View.VISIBLE);
        
        // Play bounce animation
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.success_bounce);
        ivSuccessCheck.startAnimation(bounceAnimation);
        
        // Navigate to login after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2500);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!show);
        btnResetPassword.setAlpha(show ? 0.6f : 1.0f);
    }
}
