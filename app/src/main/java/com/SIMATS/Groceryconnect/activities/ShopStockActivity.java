package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.ProductAdapter;
import com.SIMATS.Groceryconnect.models.Product;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.SIMATS.Groceryconnect.utils.SessionManager;

public class ShopStockActivity extends AppCompatActivity implements ProductAdapter.OnCartUpdateListener {

    public static final String EXTRA_SHOP_ID = "shop_id";
    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_SHOP_CITY = "shop_city";
    public static final String EXTRA_DELIVERY_AVAILABLE = "delivery_available";
    public static final String EXTRA_SHOP_IMAGE = "shop_image";

    // Views
    private ImageView ivBack, ivCart;
    private TextView tvShopName, tvCartBadge, tvEmpty;
    private EditText etSearchProducts;
    private ProgressBar progressBar;
    private RecyclerView rvProducts;
    private FrameLayout layoutCart;
    private Button btnGoToCart;

    // Category chips
    private TextView chipAll, chipFruits, chipVegetables, chipDairy, chipBakery, chipBeverages;
    private TextView[] categoryChips;
    private String selectedCategory = "All";

    // Data
    private int shopId;
    private String shopName;
    private String shopCity;
    private boolean deliveryAvailable;
    private String shopImage;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_stock);

        // Get intent data
        shopId = getIntent().getIntExtra(EXTRA_SHOP_ID, -1);
        shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        shopCity = getIntent().getStringExtra(EXTRA_SHOP_CITY);
        deliveryAvailable = getIntent().getBooleanExtra(EXTRA_DELIVERY_AVAILABLE, false);
        shopImage = getIntent().getStringExtra(EXTRA_SHOP_IMAGE);

        // Set shop info in CartManager right away (including shop image)
        com.SIMATS.Groceryconnect.utils.CartManager.getInstance().setShopInfo(
            shopId, shopName, shopCity, deliveryAvailable, shopImage);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupClickListeners();

        // Load products
        loadProducts();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivCart = findViewById(R.id.iv_cart);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        tvEmpty = findViewById(R.id.tv_empty);
        etSearchProducts = findViewById(R.id.et_search_products);
        progressBar = findViewById(R.id.progressBar);
        rvProducts = findViewById(R.id.rv_products);
        layoutCart = findViewById(R.id.layout_cart);
        btnGoToCart = findViewById(R.id.btn_go_to_cart);

        // Category chips
        chipAll = findViewById(R.id.chip_all);
        chipFruits = findViewById(R.id.chip_fruits);
        chipVegetables = findViewById(R.id.chip_vegetables);
        chipDairy = findViewById(R.id.chip_dairy);
        chipBakery = findViewById(R.id.chip_bakery);
        chipBeverages = findViewById(R.id.chip_beverages);

        categoryChips = new TextView[]{chipAll, chipFruits, chipVegetables, chipDairy, chipBakery, chipBeverages};

        // Set shop name
        if (shopName != null) {
            tvShopName.setText(shopName);
        }

        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupRecyclerView() {
        sessionManager = new SessionManager(this);
        productList = new ArrayList<>();
        int userId = sessionManager.getUserId();
        productAdapter = new ProductAdapter(productList, this, userId, this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);
    }

    private void setupSearch() {
        etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                productAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryChips() {
        String[] categories = {"All", "Fruits", "Vegetables", "Dairy", "Bakery", "Beverages"};
        
        for (int i = 0; i < categoryChips.length; i++) {
            final String category = categories[i];
            categoryChips[i].setOnClickListener(v -> {
                selectCategory(category);
            });
        }
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        
        // Update chip appearances
        for (TextView chip : categoryChips) {
            if (chip.getText().toString().equalsIgnoreCase(category)) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(0xFF000000); // Black
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(0xFFFFFFFF); // White
            }
        }

        // Filter products
        productAdapter.filterByCategory(category);
        updateEmptyState();
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        layoutCart.setOnClickListener(v -> {
            // Navigate to cart
            com.SIMATS.Groceryconnect.utils.CartManager cartManager = com.SIMATS.Groceryconnect.utils.CartManager.getInstance();
            if (cartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, CartDetailsActivity.class);
                intent.putExtra(CartDetailsActivity.EXTRA_FROM_SHOP_STOCK, true);
                startActivity(intent);
            }
        });

        // Go to Cart button
        btnGoToCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartDetailsActivity.class);
            intent.putExtra(CartDetailsActivity.EXTRA_FROM_SHOP_STOCK, true);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        showLoading(true);

        String url = ApiConfig.GET_PRODUCTS_BY_SHOP_URL + "?shop_id=" + shopId;

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
            Request.Method.GET, url,
            response -> {
                showLoading(false);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray productsArray = json.getJSONArray("products");
                        productList.clear();

                        for (int i = 0; i < productsArray.length(); i++) {
                            JSONObject productJson = productsArray.getJSONObject(i);
                            
                            // Skip products with zero stock
                            int stockQuantity = productJson.optInt("stock_quantity", 0);
                            if (stockQuantity <= 0) {
                                continue;
                            }
                            
                            Product product = new Product();
                            product.setProductId(productJson.getInt("product_id"));
                            product.setShopId(shopId);
                            product.setProductName(productJson.getString("product_name"));
                            product.setCategory(productJson.optString("category", "General"));
                            product.setPrice(productJson.getDouble("price"));
                            product.setStockQuantity(stockQuantity);
                            product.setImageUrl(productJson.optString("product_image", ""));

                            productList.add(product);
                        }

                        productAdapter.updateProducts(new java.util.ArrayList<>(productList));
                        updateEmptyState();
                        
                        // Check which items are saved
                        checkSavedItems();

                    } else {
                        android.util.Log.w("ShopStock", "API returned status false");
                        updateEmptyState();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ShopStock", "Error parsing: " + e.getMessage());
                    Toast.makeText(this, "Error parsing products", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            },
            error -> {
                showLoading(false);
                String errorMsg = "Network error";
                if (error.networkResponse != null) {
                    errorMsg += " - Status: " + error.networkResponse.statusCode;
                } else if (error.getMessage() != null) {
                    errorMsg += " - " + error.getMessage();
                }
                android.util.Log.e("ShopStock", errorMsg);
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                updateEmptyState();
            });

        requestQueue.add(request);
    }

    @Override
    public void onCartUpdated(int totalItems) {
        if (totalItems > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(totalItems));
            btnGoToCart.setVisibility(View.VISIBLE);
            btnGoToCart.setText("🛒 Go to Cart (" + totalItems + " items)");
        } else {
            tvCartBadge.setVisibility(View.GONE);
            btnGoToCart.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (productAdapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Check which products are saved and update their UI state
     */
    private void checkSavedItems() {
        if (productList.isEmpty()) return;
        
        int userId = sessionManager.getUserId();
        if (userId <= 0) return;
        
        // Build comma-separated list of product IDs
        StringBuilder productIds = new StringBuilder();
        for (int i = 0; i < productList.size(); i++) {
            if (i > 0) productIds.append(",");
            productIds.append(productList.get(i).getProductId());
        }
        
        String url = ApiConfig.CHECK_SAVED_ITEMS_URL + "?user_id=" + userId + "&product_ids=" + productIds.toString();
        
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
            Request.Method.GET, url, null,
            response -> {
                try {
                    if (response.getBoolean("success")) {
                        JSONArray savedIds = response.getJSONArray("saved_product_ids");
                        
                        // Create a set for quick lookup
                        java.util.Set<Integer> savedSet = new java.util.HashSet<>();
                        for (int i = 0; i < savedIds.length(); i++) {
                            savedSet.add(savedIds.getInt(i));
                        }
                        
                        // Update each product's saved state
                        for (Product product : productList) {
                            product.setSaved(savedSet.contains(product.getProductId()));
                        }
                        
                        // Notify adapter to refresh
                        productAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ShopStock", "Error checking saved items: " + e.getMessage());
                }
            },
            error -> {
                android.util.Log.e("ShopStock", "Error checking saved items: " + error.getMessage());
            });
        
        requestQueue.add(request);
    }
}
