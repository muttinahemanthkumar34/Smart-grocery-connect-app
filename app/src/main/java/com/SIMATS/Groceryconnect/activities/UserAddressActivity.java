package com.SIMATS.Groceryconnect.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.AddressAdapter;
import com.SIMATS.Groceryconnect.models.Address;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressActionListener {

    private ImageView ivBack;
    private RecyclerView rvAddresses;
    private Button btnAddAddress;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private AddressAdapter adapter;
    private List<Address> addressList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadAddresses();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        rvAddresses = findViewById(R.id.rv_addresses);
        btnAddAddress = findViewById(R.id.btn_add_address);
        tvEmpty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter(addressList, this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog(null));
    }

    private void loadAddresses() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        String url = ApiConfig.GET_USER_ADDRESSES_URL + "?user_id=" + sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("status")) {
                            JSONArray addressesArray = jsonResponse.getJSONArray("addresses");
                            addressList.clear();

                            for (int i = 0; i < addressesArray.length(); i++) {
                                JSONObject obj = addressesArray.getJSONObject(i);
                                Address address = new Address();
                                address.setAddressId(obj.getInt("address_id"));
                                address.setUserId(obj.getInt("user_id"));
                                address.setLabel(obj.optString("label", "Address"));
                                address.setAddressLine(obj.getString("address_line"));
                                address.setCity(obj.getString("city"));
                                address.setPincode(obj.getString("pincode"));
                                address.setDefault(obj.optInt("is_default", 0) == 1);
                                addressList.add(address);
                            }

                            adapter.updateList(addressList);
                            updateEmptyState();
                        } else {
                            updateEmptyState();
                        }
                    } catch (Exception e) {
                        Log.e("UserAddressActivity", "Error parsing address list", e);
                        updateEmptyState();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load addresses", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void updateEmptyState() {
        if (addressList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvAddresses.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvAddresses.setVisibility(View.VISIBLE);
        }
    }

    private void showAddAddressDialog(Address existingAddress) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etLabel = dialogView.findViewById(R.id.et_label);
        EditText etAddressLine = dialogView.findViewById(R.id.et_address_line);
        EditText etCity = dialogView.findViewById(R.id.et_city);
        EditText etPincode = dialogView.findViewById(R.id.et_pincode);
        CheckBox cbDefault = dialogView.findViewById(R.id.cb_default);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        boolean isEditing = existingAddress != null;
        tvTitle.setText(isEditing ? "Edit Address" : "Add New Address");

        if (isEditing) {
            etLabel.setText(existingAddress.getLabel());
            etAddressLine.setText(existingAddress.getAddressLine());
            etCity.setText(existingAddress.getCity());
            etPincode.setText(existingAddress.getPincode());
            cbDefault.setChecked(existingAddress.isDefault());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String label = etLabel.getText().toString().trim();
            String addressLine = etAddressLine.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();
            boolean isDefault = cbDefault.isChecked();

            if (label.isEmpty() || addressLine.isEmpty() || city.isEmpty() || pincode.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();

            if (isEditing) {
                updateAddress(existingAddress.getAddressId(), label, addressLine, city, pincode, isDefault);
            } else {
                addAddress(label, addressLine, city, pincode, isDefault);
            }
        });

        dialog.show();
    }

    private void addAddress(String label, String addressLine, String city, String pincode, boolean isDefault) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.ADD_ADDRESS_URL,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Address added successfully", Toast.LENGTH_SHORT).show();
                            loadAddresses();
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error adding address", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                params.put("label", label);
                params.put("address_line", addressLine);
                params.put("city", city);
                params.put("pincode", pincode);
                params.put("is_default", isDefault ? "1" : "0");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updateAddress(int addressId, String label, String addressLine, String city, String pincode, boolean isDefault) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.UPDATE_ADDRESS_URL,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
                            loadAddresses();
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error updating address", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("address_id", String.valueOf(addressId));
                params.put("label", label);
                params.put("address_line", addressLine);
                params.put("city", city);
                params.put("pincode", pincode);
                params.put("is_default", isDefault ? "1" : "0");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteAddress(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);

                    StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.DELETE_ADDRESS_URL,
                            response -> {
                                progressBar.setVisibility(View.GONE);
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    if (jsonResponse.getBoolean("success")) {
                                        Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show();
                                        loadAddresses();
                                    } else {
                                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(this, "Error deleting address", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("address_id", String.valueOf(address.getAddressId()));
                            return params;
                        }
                    };

                    Volley.newRequestQueue(this).add(request);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditClick(Address address) {
        showAddAddressDialog(address);
    }

    @Override
    public void onDeleteClick(Address address) {
        deleteAddress(address);
    }
}
