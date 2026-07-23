package com.SIMATS.Groceryconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.utils.SessionManager;

/**
 * MainActivity acts as a splash screen.
 * Displays for 3 seconds then navigates based on login status.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SPLASH_DELAY = 3000; // 3 seconds
    
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Splash screen started");
        
        sessionManager = new SessionManager(this);

        // Navigate after delay based on login status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                String role = sessionManager.getUserRole();
                Log.d(TAG, "User logged in with role: " + role + ", navigating to appropriate dashboard");
                Intent intent;
                if ("ADMIN".equalsIgnoreCase(role)) {
                    intent = new Intent(MainActivity.this, AdminDashboard.class);
                } else {
                    intent = new Intent(MainActivity.this, UserDashboard.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Log.d(TAG, "User not logged in, navigating to LoginActivity");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            finish(); // Close splash screen
        }, SPLASH_DELAY);
    }
}
