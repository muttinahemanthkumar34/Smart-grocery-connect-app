package com.SIMATS.Groceryconnect.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditUserProfile extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etName, etEmail, etPhone;
    private Button btnSaveChanges;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize views
        initViews();

        // Load current user data
        loadUserData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
    }

    private void loadUserData() {
        // Load user data from session into form fields
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        String userPhone = sessionManager.getUserPhone();

        if (userName != null && !userName.isEmpty()) {
            etName.setText(userName);
        }

        if (userEmail != null && !userEmail.isEmpty()) {
            etEmail.setText(userEmail);
        }

        if (userPhone != null && !userPhone.isEmpty()) {
            etPhone.setText(userPhone);
        }
    }

    private void setupClickListeners() {
        // Back button - return to profile
        ivBack.setOnClickListener(v -> finish());

        // Save changes button
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate input
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Saving...");

        // Make API call to update user profile
        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.UPDATE_USER_URL,
                response -> {
                    btnSaveChanges.setEnabled(true);
                    btnSaveChanges.setText("Save Changes");

                    // Log the response for debugging
                    android.util.Log.d("EditUserProfile", "API Response: " + response);

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        if (success) {
                            // Update session data
                            sessionManager.updateUserData(name, phone);

                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            // Navigate back to profile
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing response: " + response, Toast.LENGTH_LONG).show();
                        Log.e("EditUserProfile", "Error parsing response", e);
                    }
                },
                error -> {
                    btnSaveChanges.setEnabled(true);
                    btnSaveChanges.setText("Save Changes");
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("EditUserProfile", "Network error updating profile", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                params.put("name", name);
                params.put("phone", phone);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}