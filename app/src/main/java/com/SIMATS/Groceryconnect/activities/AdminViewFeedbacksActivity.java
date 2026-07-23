package com.SIMATS.Groceryconnect.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.FeedbackAdapter;
import com.SIMATS.Groceryconnect.models.Feedback;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.network.VolleySingleton;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminViewFeedbacksActivity extends AppCompatActivity {

    private static final String TAG = "AdminViewFeedbacks";

    public static final String EXTRA_SHOP_ID = "shop_id";

    // Views
    private ImageView ivBack;
    private TextView tvTotalReviews, tvAvgRating;
    private RecyclerView rvFeedbacks;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    // Data
    private int shopId;
    private FeedbackAdapter feedbackAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_feedbacks);

        sessionManager = new SessionManager(this);
        shopId = getIntent().getIntExtra(EXTRA_SHOP_ID, 0);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadFeedbacks();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvTotalReviews = findViewById(R.id.tv_total_reviews);
        tvAvgRating = findViewById(R.id.tv_avg_rating);
        rvFeedbacks = findViewById(R.id.rv_feedbacks);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        feedbackAdapter = new FeedbackAdapter();
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
        rvFeedbacks.setAdapter(feedbackAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadFeedbacks() {
        if (shopId <= 0) {
            // Try to get shop_id from a dashboard API call
            loadShopIdFirst();
            return;
        }

        fetchFeedbacks();
    }

    private void loadShopIdFirst() {
        int adminId = sessionManager.getUserId();
        if (adminId <= 0) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        String url = ApiConfig.GET_ADMIN_DASHBOARD_URL + "?admin_id=" + adminId;
        Log.d(TAG, "loadShopIdFirst: " + url);

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        shopId = json.getInt("shop_id");
                        fetchFeedbacks();
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
                Log.e(TAG, "Network error", error);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchFeedbacks() {
        showLoading(true);
        String url = ApiConfig.GET_SHOP_FEEDBACKS_URL + "?shop_id=" + shopId;
        Log.d(TAG, "fetchFeedbacks: " + url);

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                showLoading(false);
                Log.d(TAG, "Response: " + response);
                parseFeedbacks(response);
            },
            error -> {
                showLoading(false);
                Log.e(TAG, "Network error", error);
                Toast.makeText(this, "Failed to load feedbacks", Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseFeedbacks(String response) {
        try {
            JSONObject json = new JSONObject(response);

            if (json.getBoolean("status")) {
                JSONArray feedbacksArray = json.getJSONArray("feedbacks");
                List<Feedback> feedbacks = new ArrayList<>();
                int totalRating = 0;

                for (int i = 0; i < feedbacksArray.length(); i++) {
                    JSONObject obj = feedbacksArray.getJSONObject(i);
                    
                    Feedback feedback = new Feedback();
                    feedback.setUserName(obj.optString("user_name", "Anonymous"));
                    feedback.setRating(obj.optInt("rating", 5));
                    feedback.setComments(obj.optString("comments", ""));
                    feedback.setCreatedAt(obj.optString("created_at", ""));
                    
                    feedbacks.add(feedback);
                    totalRating += feedback.getRating();
                }

                // Update stats
                int totalReviews = feedbacks.size();
                tvTotalReviews.setText(String.valueOf(totalReviews));

                if (totalReviews > 0) {
                    double avgRating = (double) totalRating / totalReviews;
                    tvAvgRating.setText(String.format("%.1f", avgRating));
                } else {
                    tvAvgRating.setText("0.0");
                }

                // Update adapter
                feedbackAdapter.updateFeedbacks(feedbacks);
                updateEmptyState(feedbacks.isEmpty());

                Log.d(TAG, "Loaded " + feedbacks.size() + " feedbacks");
            } else {
                updateEmptyState(true);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse error", e);
            Toast.makeText(this, "Error parsing feedbacks", Toast.LENGTH_SHORT).show();
            updateEmptyState(true);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFeedbacks.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFeedbacks.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
