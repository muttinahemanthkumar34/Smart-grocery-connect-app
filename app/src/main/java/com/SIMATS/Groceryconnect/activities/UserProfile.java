package com.SIMATS.Groceryconnect.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private static final String TAG = "UserProfile";
    private SessionManager sessionManager;
    private RequestQueue requestQueue;
    private TextView tvUserName, tvUserEmail;
    private ImageView ivAvatar, ivCameraOverlay;
    private FrameLayout layoutAvatar;
    // Bottom Nav
    private LinearLayout navHome, navOrders, navAi, navCartText, navProfile;
    private Button btnEditProfile, btnLogout;
    private SwitchCompat switchNotifications;
    
    // Profile image handling
    private String profileImageBase64 = null;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        requestQueue = Volley.newRequestQueue(this);
        
        setupActivityLaunchers();

        // Initialize views
        initViews();

        // Load user data
        loadUserData();
        
        // Load notification setting
        loadNotificationSetting();

        // Setup click listeners
        setupClickListeners();
    }
    
    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null) {
                        profileImageBase64 = bitmapToBase64(photo);
                        ivAvatar.setImageBitmap(photo);
                        ivAvatar.setImageTintList(null);
                        uploadProfileImage();
                    }
                }
            }
        );
        
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            profileImageBase64 = bitmapToBase64(bitmap);
                            ivAvatar.setImageBitmap(bitmap);
                            ivAvatar.setImageTintList(null);
                            uploadProfileImage();
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading gallery image", e);
                        }
                    }
                }
            }
        );
        
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int maxSize = 800;
        float scale = Math.min((float) maxSize / bitmap.getWidth(), (float) maxSize / bitmap.getHeight());
        if (scale < 1) {
            int newWidth = Math.round(bitmap.getWidth() * scale);
            int newHeight = Math.round(bitmap.getHeight() * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning to this screen (e.g., after editing profile)
        loadUserData();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        switchNotifications = findViewById(R.id.switch_notifications);
        ivAvatar = findViewById(R.id.iv_avatar);
        ivCameraOverlay = findViewById(R.id.iv_camera_overlay);
        layoutAvatar = findViewById(R.id.layout_avatar);

        // Bottom Nav
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navAi = findViewById(R.id.nav_ai);
        navCartText = findViewById(R.id.nav_cart_text);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void loadUserData() {
        // Get user data from SessionManager
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        String profileImage = sessionManager.getProfileImage();

        // Set user data to views
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        } else {
            tvUserName.setText("User");
        }

        if (userEmail != null && !userEmail.isEmpty()) {
            tvUserEmail.setText(userEmail);
        } else {
            tvUserEmail.setText("email@example.com");
        }
        
        // Load profile image
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = profileImage.startsWith("http") ? profileImage : ApiConfig.IMAGE_BASE_URL + profileImage;
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(ivAvatar);
            ivAvatar.setImageTintList(null);
        }
    }

    private void setupClickListeners() {
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
            Intent intent = new Intent(this, AiAssistantActivity.class);
            startActivity(intent);
        });

        navCartText.setOnClickListener(v -> {
            com.SIMATS.Groceryconnect.utils.CartManager cartManager = 
                com.SIMATS.Groceryconnect.utils.CartManager.getInstance();
            if (cartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, CartDetailsActivity.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(v -> {
            // Already on profile
        });

        // Edit Profile button - navigate to EditUserProfile
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserProfile.class);
            startActivity(intent);
        });

        // Logout button
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Menu items
        findViewById(R.id.menu_addresses).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserAddressActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.menu_saved).setOnClickListener(v -> {
            Intent intent = new Intent(this, SavedItemsActivity.class);
            startActivity(intent);
        });

        // Notification toggle listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationSetting(isChecked);
        });

        findViewById(R.id.menu_privacy).setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.menu_terms).setOnClickListener(v -> {
            Intent intent = new Intent(this, TermsOfServiceActivity.class);
            startActivity(intent);
        });
        
        // Profile picture click listener
        View.OnClickListener profilePicClickListener = v -> showImagePickerDialog();
        ivAvatar.setOnClickListener(profilePicClickListener);
        ivCameraOverlay.setOnClickListener(profilePicClickListener);
        layoutAvatar.setOnClickListener(profilePicClickListener);
    }
    
    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        new AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndOpen();
                } else if (which == 1) {
                    openGallery();
                }
            })
            .show();
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    
    private void uploadProfileImage() {
        if (profileImageBase64 == null) return;
        
        Toast.makeText(this, "Updating profile picture...", Toast.LENGTH_SHORT).show();
        
        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.UPDATE_USER_URL,
            response -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    
                    if (success) {
                        String profileImage = jsonResponse.optString("profile_image", "");
                        if (!profileImage.isEmpty()) {
                            sessionManager.setProfileImage(profileImage);
                        }
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            },
            error -> {
                Log.e(TAG, "Network error", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                params.put("name", sessionManager.getUserName());
                params.put("phone", sessionManager.getUserPhone());
                params.put("profile_image", profileImageBase64);
                return params;
            }
        };
        
        requestQueue.add(request);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> performLogout())
            .setNegativeButton("No", null)
            .show();
    }

    private void performLogout() {
        // Clear session
        sessionManager.logout();

        // Clear cart
        com.SIMATS.Groceryconnect.utils.CartManager.getInstance().clearCart();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Load notification setting from server
     */
    private void loadNotificationSetting() {
        int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_NOTIFICATION_SETTING_URL + "?user_id=" + userId;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    if (response.getBoolean("status")) {
                        boolean enabled = response.getBoolean("notifications_enabled");
                        // Update switch without triggering listener
                        switchNotifications.setOnCheckedChangeListener(null);
                        switchNotifications.setChecked(enabled);
                        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            updateNotificationSetting(isChecked);
                        });
                        // Save locally
                        sessionManager.setNotificationsEnabled(enabled);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing notification setting", e);
                }
            },
            error -> {
                Log.e(TAG, "Error loading notification setting", error);
                // Use local preference as fallback
                switchNotifications.setChecked(sessionManager.isNotificationsEnabled());
            });
        
        requestQueue.add(request);
    }
    
    /**
     * Update notification setting on server
     */
    private void updateNotificationSetting(boolean enabled) {
        int userId = sessionManager.getUserId();
        
        StringRequest request = new StringRequest(Request.Method.POST,
            ApiConfig.UPDATE_NOTIFICATION_SETTING_URL,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        sessionManager.setNotificationsEnabled(enabled);
                        String message = enabled ? "Notifications enabled" : "Notifications disabled";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update setting", Toast.LENGTH_SHORT).show();
                        // Revert switch
                        switchNotifications.setChecked(!enabled);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            },
            error -> {
                Log.e(TAG, "Error updating notification setting", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                // Revert switch
                switchNotifications.setChecked(!enabled);
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("notifications_enabled", enabled ? "1" : "0");
                return params;
            }
        };
        
        requestQueue.add(request);
    }
}