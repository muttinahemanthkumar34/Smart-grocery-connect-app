package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OtpVerificationActivity handles OTP input and verification via backend API.
 */
public class OtpVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OtpVerification";

    // Views
    private ImageView ivBack;
    private TextView tvTitle, tvSubtitle, tvResend, tvTimer, tvChangeNumber;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify;
    private ProgressBar progressBar;

    // Data
    private String email;
    private String role = "USER";
    private CountDownTimer countDownTimer;

    // Colors
    private int accentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get data from intent
        email = getIntent().getStringExtra("EMAIL");
        role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "USER";

        initViews();
        setupTheme();
        setupOtpInputs();
        setupClickListeners();
        startCountdownTimer();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvResend = findViewById(R.id.tv_resend);
        tvTimer = findViewById(R.id.tv_timer);
        tvChangeNumber = findViewById(R.id.tv_change_number);
        btnVerify = findViewById(R.id.btn_verify);
        progressBar = findViewById(R.id.progressBar);

        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);

        // Update subtitle with masked email
        String maskedEmail = maskEmail(email);
        tvSubtitle.setText("Enter the 6-digit code sent to\n" + maskedEmail);
        
        // Update "Change Number" text to "Change Email"
        tvChangeNumber.setText("Change Email");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name + "***@" + domain;
        }
        return name.substring(0, 2) + "***@" + domain;
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
        tvResend.setTextColor(accentColor);
        tvChangeNumber.setTextColor(accentColor);
        btnVerify.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(accentColor));
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};
        Animation focusAnimation = AnimationUtils.loadAnimation(this, R.anim.otp_box_focus);

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            EditText field = otpFields[i];

            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        // Move to next field
                        otpFields[index + 1].requestFocus();
                        otpFields[index + 1].startAnimation(focusAnimation);
                    } else if (s.length() == 0 && index > 0) {
                        // Move to previous field on delete
                        otpFields[index - 1].requestFocus();
                    }
                    
                    // Auto-verify when all fields are filled
                    if (isOtpComplete()) {
                        verifyOtp();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Handle backspace on empty field
            field.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL 
                    && field.getText().length() == 0 
                    && index > 0) {
                    otpFields[index - 1].requestFocus();
                    otpFields[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }

        // Focus first field
        etOtp1.requestFocus();
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        tvChangeNumber.setOnClickListener(v -> finish());
        
        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                resendOtp();
            }
        });
        
        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void startCountdownTimer() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);
        
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format("00:%02d", seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                tvResend.setEnabled(true);
                tvResend.setAlpha(1.0f);
            }
        }.start();
    }

    private boolean isOtpComplete() {
        return etOtp1.getText().length() == 1
            && etOtp2.getText().length() == 1
            && etOtp3.getText().length() == 1
            && etOtp4.getText().length() == 1
            && etOtp5.getText().length() == 1
            && etOtp6.getText().length() == 1;
    }

    private String getOtpCode() {
        return etOtp1.getText().toString()
            + etOtp2.getText().toString()
            + etOtp3.getText().toString()
            + etOtp4.getText().toString()
            + etOtp5.getText().toString()
            + etOtp6.getText().toString();
    }

    private void verifyOtp() {
        if (!isOtpComplete()) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        String otpCode = getOtpCode();
        Log.d(TAG, "Verifying OTP: " + otpCode);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.VERIFY_EMAIL_OTP_URL,
            response -> {
                showLoading(false);
                Log.d(TAG, "Verify OTP response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                        navigateToResetPassword();
                    } else {
                        String message = json.optString("message", "Invalid OTP");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        clearOtpFields();
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
                params.put("otp", otpCode);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void resendOtp() {
        showLoading(true);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.SEND_EMAIL_OTP_URL,
            response -> {
                showLoading(false);
                Log.d(TAG, "Resend OTP response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "OTP resent successfully!", Toast.LENGTH_SHORT).show();
                        startCountdownTimer();
                        clearOtpFields();
                    } else {
                        String message = json.optString("message", "Failed to resend OTP");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Parse error", e);
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Network error", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
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

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void navigateToResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("EMAIL", email);
        intent.putExtra("ROLE", role);
        intent.putExtra("VERIFIED", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
        btnVerify.setAlpha(show ? 0.6f : 1.0f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
