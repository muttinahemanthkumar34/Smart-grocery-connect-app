package com.SIMATS.Groceryconnect.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.AddressSelectAdapter;
import com.SIMATS.Groceryconnect.adapters.CartItemAdapter;
import com.SIMATS.Groceryconnect.models.Address;
import com.SIMATS.Groceryconnect.models.CartItem;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.CartManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartDetailsActivity extends AppCompatActivity implements CartItemAdapter.OnCartChangeListener {

    public static final String EXTRA_FROM_SHOP_STOCK = "from_shop_stock";
    private static final double DELIVERY_FEE = 50.00;

    // Views
    private ImageView ivBack, ivStoreImage;
    private LinearLayout navHome, navOrders, navAi, navCartText, navProfile;
    private TextView tvShopName, tvShopCity;
    private RecyclerView rvCartItems;
    private TextView tvEmptyCart;
    private TextView tvDelivery, tvPickup;
    private LinearLayout layoutAddress, layoutDeliveryFee, layoutDeliveryToggle;
    private TextView tvAddress;
    private EditText etNotes;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private Button btnPlaceOrder;
    private ImageView ivPaymentIcon;
    private TextView tvPaymentMethod, tvPaymentDesc;

    // Data
    private CartManager cartManager;
    private CartItemAdapter cartAdapter;
    private boolean isDeliverySelected = true;
    private String userAddress = "";
    private int userId;
    private RequestQueue requestQueue;
    private boolean fromShopStock = false;
    private String selectedPaymentMethod = "COD"; // COD or UPI
    private String shopUpiId = "";
    private ActivityResultLauncher<Intent> upiLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_details);

        cartManager = CartManager.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        // Get user ID from SessionManager
        com.SIMATS.Groceryconnect.utils.SessionManager sessionManager = 
            new com.SIMATS.Groceryconnect.utils.SessionManager(this);
        userId = sessionManager.getUserId();

        setupUpiLauncher();
        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadUserAddress();
        updateUI();

        // Check if coming from shop stock to show back button
        fromShopStock = getIntent().getBooleanExtra(EXTRA_FROM_SHOP_STOCK, false);
        if (fromShopStock) {
            ivBack.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivStoreImage = findViewById(R.id.iv_store_image);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvShopCity = findViewById(R.id.tv_shop_city);
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        tvDelivery = findViewById(R.id.tv_delivery);
        tvPickup = findViewById(R.id.tv_pickup);
        layoutAddress = findViewById(R.id.layout_address);
        layoutDeliveryFee = findViewById(R.id.layout_delivery_fee);
        layoutDeliveryToggle = findViewById(R.id.layout_delivery_toggle);
        tvAddress = findViewById(R.id.tv_address);
        etNotes = findViewById(R.id.et_notes);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTotal = findViewById(R.id.tv_total);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        ivPaymentIcon = findViewById(R.id.iv_payment_icon);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvPaymentDesc = findViewById(R.id.tv_payment_desc);

        // Set shop info
        tvShopName.setText("Your order from " + cartManager.getShopName());
        tvShopCity.setText(cartManager.getShopCity());

        // Load shop image
        String shopImage = cartManager.getShopImage();
        if (shopImage != null && !shopImage.isEmpty()) {
            String imageUrl = shopImage.startsWith("http") ? shopImage : ApiConfig.IMAGE_BASE_URL + shopImage;
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_shop_placeholder)
                    .error(R.drawable.img_shop_placeholder)
                    .centerCrop()
                    .into(ivStoreImage);
        }

        // Setup delivery/pickup toggle based on shop's delivery availability
        if (!cartManager.isDeliveryAvailable()) {
            // Only pickup available
            tvDelivery.setVisibility(View.GONE);
            isDeliverySelected = false;
            selectPickup();
        }

        // Bottom Nav
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navAi = findViewById(R.id.nav_ai);
        navCartText = findViewById(R.id.nav_cart_text);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupRecyclerView() {
        List<CartItem> items = cartManager.getCartItems();
        cartAdapter = new CartItemAdapter(items, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        // Back button (only shown when coming from shop stock)
        ivBack.setOnClickListener(v -> finish());

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserOrderHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navAi.setOnClickListener(v -> {
            Toast.makeText(this, "AI Assistant coming soon!", Toast.LENGTH_SHORT).show();
        });

        navCartText.setOnClickListener(v -> {
            // Already on cart
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Delivery toggle
        tvDelivery.setOnClickListener(v -> selectDelivery());
        tvPickup.setOnClickListener(v -> selectPickup());

        // Change address
        findViewById(R.id.tv_change_address).setOnClickListener(v -> showAddressSelectionDialog());

        // Change payment method
        findViewById(R.id.tv_change_payment).setOnClickListener(v -> showPaymentMethodDialog());
        findViewById(R.id.layout_payment_method).setOnClickListener(v -> showPaymentMethodDialog());

        // Place order
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void selectDelivery() {
        isDeliverySelected = true;
        tvDelivery.setBackgroundResource(R.drawable.bg_chip_selected);
        tvDelivery.setTextColor(0xFF000000);
        tvPickup.setBackgroundResource(0);
        tvPickup.setTextColor(0xFFAAAAAA);

        // Show address and delivery fee
        layoutAddress.setVisibility(View.VISIBLE);
        layoutDeliveryFee.setVisibility(View.VISIBLE);
        updateTotals();
    }

    private void selectPickup() {
        isDeliverySelected = false;
        tvPickup.setBackgroundResource(R.drawable.bg_chip_selected);
        tvPickup.setTextColor(0xFF000000);
        tvDelivery.setBackgroundResource(0);
        tvDelivery.setTextColor(0xFFAAAAAA);

        // Hide address and delivery fee
        layoutAddress.setVisibility(View.GONE);
        layoutDeliveryFee.setVisibility(View.GONE);
        updateTotals();
    }

    private void loadUserAddress() {
        String url = ApiConfig.GET_USER_DEFAULT_ADDRESS_URL + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        // API returns fields at root level, not nested
                        String addressLine = json.optString("address", "");
                        String city = json.optString("city", "");
                        String pincode = json.optString("pincode", "");
                        userAddress = addressLine + ", " + city + " - " + pincode;
                        tvAddress.setText(userAddress);
                    } else {
                        tvAddress.setText("Add delivery address");
                    }
                } catch (Exception e) {
                    tvAddress.setText("Add delivery address");
                }
            },
            error -> tvAddress.setText("Add delivery address"));

        requestQueue.add(request);
    }

    private void updateUI() {
        List<CartItem> items = cartManager.getCartItems();

        if (items.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(false);
            btnPlaceOrder.setAlpha(0.5f);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setAlpha(1.0f);
        }

        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartManager.getSubtotal();
        double deliveryFee = isDeliverySelected ? DELIVERY_FEE : 0;
        double total = subtotal + deliveryFee;

        tvSubtotal.setText(String.format("₹%.2f", subtotal));
        tvDeliveryFee.setText(String.format("₹%.2f", deliveryFee));
        tvTotal.setText(String.format("₹%.2f", total));
    }

    @Override
    public void onCartChanged() {
        cartAdapter.updateItems(cartManager.getCartItems());
        updateUI();
    }

    private void placeOrder() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDeliverySelected && userAddress.isEmpty()) {
            Toast.makeText(this, "Please add delivery address", Toast.LENGTH_SHORT).show();
            return;
        }

        // If UPI payment selected, initiate UPI flow first
        if (selectedPaymentMethod.equals("UPI")) {
            initiateUpiPayment();
            return;
        }

        // For COD, proceed with order placement
        proceedWithOrder();
    }

    private void proceedWithOrder() {
        // Disable button while processing
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Placing Order...");

        String url = ApiConfig.PLACE_ORDER_URL;
        
        // Calculate total for passing to OrderPlaced
        final double finalTotal = cartManager.getSubtotal() + (isDeliverySelected ? DELIVERY_FEE : 0);
        final String shopName = cartManager.getShopName();
        final String orderType = isDeliverySelected ? "DELIVERY" : "PICKUP";

        StringRequest request = new StringRequest(Request.Method.POST, url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        // Get order details from response
                        int orderId = json.optInt("order_id", -1);
                        String orderNumber = json.optString("order_number", "");
                        
                        // Clear cart before navigating
                        cartManager.clearCart();
                        
                        // Navigate to OrderPlaced activity
                        Intent intent = new Intent(CartDetailsActivity.this, OrderPlaced.class);
                        intent.putExtra(OrderPlaced.EXTRA_ORDER_ID, orderId);
                        intent.putExtra(OrderPlaced.EXTRA_ORDER_NUMBER, orderNumber);
                        intent.putExtra(OrderPlaced.EXTRA_STORE_NAME, shopName);
                        intent.putExtra(OrderPlaced.EXTRA_ORDER_TYPE, orderType);
                        intent.putExtra(OrderPlaced.EXTRA_TOTAL_AMOUNT, finalTotal);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = json.optString("message", "Failed to place order");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("Place Order");
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error processing order", Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place Order");
                }
            },
            error -> {
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText("Place Order");
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("shop_id", String.valueOf(cartManager.getShopId()));
                params.put("order_type", isDeliverySelected ? "DELIVERY" : "PICKUP");
                params.put("total_amount", String.valueOf(cartManager.getSubtotal() + (isDeliverySelected ? DELIVERY_FEE : 0)));
                params.put("notes", etNotes.getText().toString().trim());
                params.put("payment_method", selectedPaymentMethod);

                // Build items JSON
                JSONArray itemsArray = new JSONArray();
                try {
                    for (CartItem item : cartManager.getCartItems()) {
                        JSONObject itemObj = new JSONObject();
                        itemObj.put("product_id", item.getProduct().getProductId());
                        itemObj.put("quantity", item.getQuantity());
                        itemObj.put("price", item.getProduct().getPrice());
                        itemsArray.put(itemObj);
                    }
                } catch (Exception e) {
                    Log.e("CartDetailsActivity", "Error putting items into JSON", e);
                }
                params.put("items", itemsArray.toString());

                return params;
            }
        };

        requestQueue.add(request);
    }

    private void showAddressSelectionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_address, null);
        RecyclerView rvAddresses = dialogView.findViewById(R.id.rv_addresses);
        TextView tvNoAddresses = dialogView.findViewById(R.id.tv_no_addresses);
        Button btnAddNew = dialogView.findViewById(R.id.btn_add_new);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Load addresses
        String url = ApiConfig.GET_USER_ADDRESSES_URL + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("status")) {
                            JSONArray addressesArray = json.getJSONArray("addresses");
                            List<Address> addresses = new ArrayList<>();

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
                                addresses.add(address);
                            }

                            if (addresses.isEmpty()) {
                                rvAddresses.setVisibility(View.GONE);
                                tvNoAddresses.setVisibility(View.VISIBLE);
                            } else {
                                rvAddresses.setVisibility(View.VISIBLE);
                                tvNoAddresses.setVisibility(View.GONE);

                                AddressSelectAdapter adapter = new AddressSelectAdapter(addresses, address -> {
                                    // Update selected address
                                    userAddress = address.getAddressLine() + ", " + address.getCity() + " - " + address.getPincode();
                                    tvAddress.setText(userAddress);
                                    dialog.dismiss();
                                });

                                rvAddresses.setLayoutManager(new LinearLayoutManager(this));
                                rvAddresses.setAdapter(adapter);
                            }
                        } else {
                            rvAddresses.setVisibility(View.GONE);
                            tvNoAddresses.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        tvNoAddresses.setVisibility(View.VISIBLE);
                        tvNoAddresses.setText("Error loading addresses");
                    }
                },
                error -> {
                    tvNoAddresses.setVisibility(View.VISIBLE);
                    tvNoAddresses.setText("Network error");
                });

        requestQueue.add(request);

        // Add new address button
        btnAddNew.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, UserAddressActivity.class);
            startActivity(intent);
        });

        dialog.show();
    }

    private void setupUpiLauncher() {
        upiLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Safely reset button state first
                runOnUiThread(() -> {
                    if (btnPlaceOrder != null) {
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("Place Order");
                    }
                });
                
                try {
                    if (result.getResultCode() == RESULT_OK) {
                        // UPI payment completed (may be success or failure)
                        Intent data = result.getData();
                        if (data != null) {
                            String response = data.getStringExtra("response");
                            if (response != null && response.toLowerCase().contains("success")) {
                                // Payment successful, place the order
                                Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
                                proceedWithOrder();
                                return;
                            }
                        }
                    }
                    // Any other case: cancelled, failed, back pressed
                    Toast.makeText(this, "Payment cancelled. You can try again.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CartDetailsActivity", "Error handling payment result", e);
                    Toast.makeText(this, "Payment was interrupted", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void showPaymentMethodDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_method, null);
        
        LinearLayout optionCod = dialogView.findViewById(R.id.option_cod);
        LinearLayout optionUpi = dialogView.findViewById(R.id.option_upi);
        ImageView ivCodCheck = dialogView.findViewById(R.id.iv_cod_check);
        ImageView ivUpiCheck = dialogView.findViewById(R.id.iv_upi_check);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // Track selection in dialog
        final String[] tempSelection = {selectedPaymentMethod};

        // Update check marks based on current selection
        Runnable updateChecks = () -> {
            ivCodCheck.setVisibility(tempSelection[0].equals("COD") ? View.VISIBLE : View.GONE);
            ivUpiCheck.setVisibility(tempSelection[0].equals("UPI") ? View.VISIBLE : View.GONE);
        };
        updateChecks.run();

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DarkDialogTheme)
                .setView(dialogView)
                .create();

        optionCod.setOnClickListener(v -> {
            tempSelection[0] = "COD";
            updateChecks.run();
        });

        optionUpi.setOnClickListener(v -> {
            tempSelection[0] = "UPI";
            updateChecks.run();
        });

        btnConfirm.setOnClickListener(v -> {
            selectedPaymentMethod = tempSelection[0];
            updatePaymentMethodUI();
            dialog.dismiss();
        });

        dialog.show();
        
        // Set dialog width to 90% of screen width
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void updatePaymentMethodUI() {
        if (selectedPaymentMethod.equals("COD")) {
            ivPaymentIcon.setImageResource(R.drawable.ic_cash);
            ivPaymentIcon.setColorFilter(0xFF10B981); // Green
            tvPaymentMethod.setText("Cash On Delivery");
            tvPaymentDesc.setText("Pay when you receive");
        } else {
            ivPaymentIcon.setImageResource(R.drawable.ic_upi);
            ivPaymentIcon.setColorFilter(0xFF6366F1); // Purple/Indigo
            tvPaymentMethod.setText("UPI Payment");
            tvPaymentDesc.setText("Pay using any UPI app");
        }
    }

    private void initiateUpiPayment() {
        // First fetch shop UPI ID
        String url = ApiConfig.GET_SHOP_UPI_URL + "?shop_id=" + cartManager.getShopId();
        
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Initiating Payment...");

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        shopUpiId = json.optString("upi_id", "");
                        if (shopUpiId.isEmpty()) {
                            Toast.makeText(this, "Shop has not set up UPI. Please use Cash On Delivery.", Toast.LENGTH_LONG).show();
                            btnPlaceOrder.setEnabled(true);
                            btnPlaceOrder.setText("Place Order");
                            return;
                        }
                        
                        // Launch UPI payment
                        launchUpiPayment();
                    } else {
                        Toast.makeText(this, "Could not fetch payment details", Toast.LENGTH_SHORT).show();
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("Place Order");
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error fetching payment details", Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place Order");
                }
            },
            error -> {
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText("Place Order");
            });

        requestQueue.add(request);
    }

    private void launchUpiPayment() {
        double amount = cartManager.getSubtotal() + (isDeliverySelected ? DELIVERY_FEE : 0);
        String shopName = cartManager.getShopName();
        String transactionNote = "Payment for order at " + shopName;

        // Build UPI URI
        // Format: upi://pay?pa=UPI_ID&pn=PAYEE_NAME&am=AMOUNT&cu=INR&tn=NOTE
        Uri uri = Uri.parse("upi://pay")
                .buildUpon()
                .appendQueryParameter("pa", shopUpiId)
                .appendQueryParameter("pn", shopName)
                .appendQueryParameter("am", String.format("%.2f", amount))
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("tn", transactionNote)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        try {
            // Show app chooser for UPI apps
            Intent chooser = Intent.createChooser(intent, "Pay with");
            upiLauncher.launch(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No UPI app found. Please install a UPI payment app.", Toast.LENGTH_LONG).show();
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText("Place Order");
        }
    }
}
