package com.SIMATS.Groceryconnect.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.AdminProductAdapter;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminStockManageActivity extends AppCompatActivity implements AdminProductAdapter.OnProductActionListener {

    private static final String TAG = "AdminStockManage";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private ImageView ivBack;
    private EditText etSearch;
    private RecyclerView rvProducts;
    private LinearLayout layoutEmpty, layoutCategories;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddProduct;

    // Data
    private SessionManager sessionManager;
    private AdminProductAdapter adapter;
    private List<AdminProductAdapter.AdminProductItem> productList = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private int shopId = 0;
    private String selectedCategory = "All";

    // For image picking
    private String selectedImageBase64 = null;
    private ImageView dialogImageView = null;
    private AdminProductAdapter.AdminProductItem editingProduct = null;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    // For auto-opening edit dialog from notification
    private int pendingEditProductId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stock_manage);

        sessionManager = new SessionManager(this);
        
        // Check if we should auto-open a product edit dialog (from stock notification)
        pendingEditProductId = getIntent().getIntExtra("edit_product_id", -1);

        initViews();
        setupImagePicker();
        setupRecyclerView();
        setupSearch();
        setupClickListeners();
        loadShopId();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etSearch = findViewById(R.id.et_search_products);
        rvProducts = findViewById(R.id.rv_products);
        layoutEmpty = findViewById(R.id.layout_empty);
        layoutCategories = findViewById(R.id.layout_categories);
        progressBar = findViewById(R.id.progressBar);
        fabAddProduct = findViewById(R.id.fab_add_product);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            // Resize image to save bandwidth
                            bitmap = resizeBitmap(bitmap, 500);
                            selectedImageBase64 = bitmapToBase64(bitmap);
                            
                            // Update dialog image view
                            if (dialogImageView != null) {
                                dialogImageView.setImageBitmap(bitmap);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image", e);
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(productList, this);
        adapter.setBaseImageUrl(ApiConfig.BASE_URL);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        fabAddProduct.setOnClickListener(v -> showAddEditProductDialog(null));
    }

    private void loadShopId() {
        int adminId = sessionManager.getUserId();
        if (adminId <= 0) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        String url = ApiConfig.GET_ADMIN_DASHBOARD_URL + "?admin_id=" + adminId;

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        shopId = json.getInt("shop_id");
                        loadProducts();
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Failed to get shop info", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    showLoading(false);
                    Log.e(TAG, "Parse error", e);
                }
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Error loading shop", error);
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadProducts() {
        if (shopId <= 0) return;

        showLoading(true);
        String url = ApiConfig.GET_SHOP_PRODUCTS_URL + "?shop_id=" + shopId;

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "Products response: " + response);
                parseProducts(response);
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Error loading products", error);
                Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseProducts(String response) {
        productList.clear();
        categories.clear();
        categories.add("All");

        try {
            JSONObject json = new JSONObject(response);
            
            if (json.getBoolean("status")) {
                // Parse categories
                JSONArray catsArray = json.getJSONArray("categories");
                for (int i = 0; i < catsArray.length(); i++) {
                    String cat = catsArray.getString(i);
                    if (!categories.contains(cat)) {
                        categories.add(cat);
                    }
                }
                
                // Setup category chips
                setupCategoryChips();

                // Parse products
                JSONArray productsArray = json.getJSONArray("products");
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject productObj = productsArray.getJSONObject(i);
                    
                    AdminProductAdapter.AdminProductItem product = new AdminProductAdapter.AdminProductItem();
                    product.setProductId(productObj.getInt("product_id"));
                    product.setProductName(productObj.getString("product_name"));
                    product.setCategory(productObj.optString("category", "General"));
                    product.setPrice(productObj.getDouble("price"));
                    product.setStockQuantity(productObj.getInt("stock_quantity"));
                    product.setProductImage(productObj.optString("product_image", ""));
                    product.setLowStock(productObj.optBoolean("is_low_stock", false));
                    
                    productList.add(product);
                }

                adapter.updateProducts(productList);
                
                // Check if we need to auto-open edit dialog for a specific product
                if (pendingEditProductId > 0) {
                    for (AdminProductAdapter.AdminProductItem product : productList) {
                        if (product.getProductId() == pendingEditProductId) {
                            showAddEditProductDialog(product);
                            pendingEditProductId = -1; // Reset so it doesn't open again
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse error", e);
        }

        updateEmptyState();
    }

    private void setupCategoryChips() {
        layoutCategories.removeAllViews();
        
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            
            TextView chip = new TextView(this);
            chip.setText(category);
            chip.setPadding(40, 20, 40, 20);
            chip.setTextSize(14);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) params.leftMargin = 16;
            chip.setLayoutParams(params);
            
            updateChipStyle(chip, category.equals(selectedCategory));
            
            chip.setOnClickListener(v -> {
                selectedCategory = category;
                adapter.filterByCategory(category);
                updateAllChipStyles();
                updateEmptyState();
            });
            
            layoutCategories.addView(chip);
        }
    }

    private void updateAllChipStyles() {
        for (int i = 0; i < layoutCategories.getChildCount(); i++) {
            View child = layoutCategories.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                updateChipStyle(chip, chip.getText().toString().equals(selectedCategory));
            }
        }
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_orange_selected);
            chip.setTextColor(Color.BLACK);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected_dark);
            chip.setTextColor(Color.WHITE);
        }
    }

    private void showAddEditProductDialog(AdminProductAdapter.AdminProductItem product) {
        editingProduct = product;
        selectedImageBase64 = null;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        // Get views
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        CardView cardImage = dialogView.findViewById(R.id.card_product_image);
        dialogImageView = dialogView.findViewById(R.id.iv_product_image);
        EditText etName = dialogView.findViewById(R.id.et_product_name);
        EditText etCategory = dialogView.findViewById(R.id.et_category);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etQuantity = dialogView.findViewById(R.id.et_stock_quantity);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        
        // Set title
        tvTitle.setText(product == null ? "Add New Product" : "Edit Product");
        
        // Pre-fill if editing
        if (product != null) {
            etName.setText(product.getProductName());
            etCategory.setText(product.getCategory());
            etPrice.setText(String.valueOf(product.getPrice()));
            etQuantity.setText(String.valueOf(product.getStockQuantity()));
            
            // Load existing image
            if (product.getProductImage() != null && !product.getProductImage().isEmpty()) {
                String imageUrl = product.getProductImage().startsWith("http") 
                    ? product.getProductImage() 
                    : ApiConfig.BASE_URL + product.getProductImage();
                com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(dialogImageView);
            }
        }
        
        // Image picker
        cardImage.setOnClickListener(v -> {
            if (checkPermission()) {
                openImagePicker();
            } else {
                requestPermission();
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            
            // Validation
            if (name.isEmpty()) {
                etName.setError("Required");
                return;
            }
            if (category.isEmpty()) {
                etCategory.setError("Required");
                return;
            }
            if (priceStr.isEmpty()) {
                etPrice.setError("Required");
                return;
            }
            if (quantityStr.isEmpty()) {
                etQuantity.setError("Required");
                return;
            }
            
            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);
            
            dialog.dismiss();
            
            if (product == null) {
                addProduct(name, category, price, quantity);
            } else {
                updateProduct(product.getProductId(), name, category, price, quantity);
            }
        });
        
        dialog.show();
    }

    private void showRestockDialog(AdminProductAdapter.AdminProductItem product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Restock " + product.getProductName());
        
        final EditText input = new EditText(this);
        input.setHint("Enter quantity to add");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        builder.setView(input);
        
        builder.setPositiveButton("Add Stock", (dialog, which) -> {
            String qtyStr = input.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                int addQty = Integer.parseInt(qtyStr);
                int newQty = product.getStockQuantity() + addQty;
                updateProduct(product.getProductId(), null, null, -1, newQty);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addProduct(String name, String category, double price, int quantity) {
        showLoading(true);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.ADD_PRODUCT_URL,
            response -> {
                showLoading(false);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, json.optString("message", "Failed to add product"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Parse error", e);
                }
            },
            error -> {
                showLoading(false);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shop_id", String.valueOf(shopId));
                params.put("product_name", name);
                params.put("category", category);
                params.put("price", String.valueOf(price));
                params.put("stock_quantity", String.valueOf(quantity));
                if (selectedImageBase64 != null) {
                    params.put("product_image", "data:image/jpeg;base64," + selectedImageBase64);
                }
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateProduct(int productId, String name, String category, double price, int quantity) {
        showLoading(true);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_PRODUCT_URL,
            response -> {
                showLoading(false);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, json.optString("message", "Failed to update product"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Parse error", e);
                }
            },
            error -> {
                showLoading(false);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("product_id", String.valueOf(productId));
                if (name != null) params.put("product_name", name);
                if (category != null) params.put("category", category);
                if (price >= 0) params.put("price", String.valueOf(price));
                if (quantity >= 0) params.put("stock_quantity", String.valueOf(quantity));
                if (selectedImageBase64 != null) {
                    params.put("product_image", "data:image/jpeg;base64," + selectedImageBase64);
                }
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void deleteProduct(AdminProductAdapter.AdminProductItem product) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete \"" + product.getProductName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                showLoading(true);
                
                StringRequest request = new StringRequest(
                    Request.Method.POST,
                    ApiConfig.DELETE_PRODUCT_URL,
                    response -> {
                        showLoading(false);
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("status")) {
                                Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            } else {
                                Toast.makeText(this, json.optString("message", "Failed to delete"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Parse error", e);
                        }
                    },
                    error -> {
                        showLoading(false);
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("product_id", String.valueOf(product.getProductId()));
                        params.put("shop_id", String.valueOf(shopId));
                        return params;
                    }
                };
                
                VolleySingleton.getInstance(this).addToRequestQueue(request);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onEditProduct(AdminProductAdapter.AdminProductItem product) {
        showAddEditProductDialog(product);
    }

    @Override
    public void onRestockProduct(AdminProductAdapter.AdminProductItem product) {
        showRestockDialog(product);
    }

    @Override
    public void onDeleteProduct(AdminProductAdapter.AdminProductItem product) {
        deleteProduct(product);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission required to select images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
