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
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditAdminProfile extends AppCompatActivity {

    private static final String TAG = "EditAdminProfile";

    // Views
    private ImageView ivBack;
    private EditText etName, etPhone, etShopName, etShopAddress, etCity;
    private Button btnSaveChanges;
    private ProgressBar progressBar;

    // Data
    private SessionManager sessionManager;
    private int shopId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_admin_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        initViews();
        loadData();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etShopName = findViewById(R.id.et_shop_name);
        etShopAddress = findViewById(R.id.et_shop_address);
        etCity = findViewById(R.id.et_city);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadData() {
        // Load from session
        String name = sessionManager.getUserName();
        String phone = sessionManager.getUserPhone();

        etName.setText(name != null ? name : "");
        etPhone.setText(phone != null ? phone : "");

        // Load from intent extras (shop data)
        shopId = getIntent().getIntExtra("shop_id", 0);
        String shopName = getIntent().getStringExtra("shop_name");
        String shopAddress = getIntent().getStringExtra("shop_address");
        String city = getIntent().getStringExtra("shop_city");

        etShopName.setText(shopName != null ? shopName : "");
        etShopAddress.setText(shopAddress != null ? shopAddress : "");
        etCity.setText(city != null ? city : "");
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Save changes button
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String shopAddress = etShopAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (shopAddress.isEmpty()) {
            etShopAddress.setError("Shop address is required");
            etShopAddress.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        // Show loading
        showLoading(true);
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Saving...");

        // Make API call to update profile
        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_ADMIN_PROFILE_URL,
            response -> {
                showLoading(false);
                btnSaveChanges.setEnabled(true);
                btnSaveChanges.setText("Save Changes");

                Log.d(TAG, "saveChanges: Response = " + response);

                try {
                    JSONObject json = new JSONObject(response);
                    boolean success = json.optBoolean("success", false);
                    String message = json.optString("message", "Update completed");

                    if (success) {
                        // Update session data
                        sessionManager.updateUserData(name, phone);

                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "saveChanges: Parse error", e);
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                showLoading(false);
                btnSaveChanges.setEnabled(true);
                btnSaveChanges.setText("Save Changes");

                Log.e(TAG, "saveChanges: Error = " + error.getMessage());
                Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("admin_id", String.valueOf(sessionManager.getUserId()));
                params.put("shop_id", String.valueOf(shopId));
                params.put("name", name);
                params.put("phone", phone);
                params.put("shop_address", shopAddress);
                params.put("city", city);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
