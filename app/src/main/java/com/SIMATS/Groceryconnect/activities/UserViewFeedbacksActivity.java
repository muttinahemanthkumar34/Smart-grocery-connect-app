package com.SIMATS.Groceryconnect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.FeedbackAdapter;
import com.SIMATS.Groceryconnect.models.Feedback;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserViewFeedbacksActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvShopName, tvAvgRating, tvTotalReviews, tvEmpty;
    private RecyclerView rvFeedbacks;
    private ProgressBar progressBar;

    private int shopId;
    private String shopName;
    private RequestQueue requestQueue;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view_feedbacks);

        shopId = getIntent().getIntExtra("shop_id", -1);
        shopName = getIntent().getStringExtra("shop_name");

        initViews();
        setupRecyclerView();
        loadFeedbacks();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvAvgRating = findViewById(R.id.tv_avg_rating);
        tvTotalReviews = findViewById(R.id.tv_total_reviews);
        tvEmpty = findViewById(R.id.tv_empty);
        rvFeedbacks = findViewById(R.id.rv_feedbacks);
        progressBar = findViewById(R.id.progressBar);

        if (shopName != null) {
            tvShopName.setText(shopName);
        }

        requestQueue = Volley.newRequestQueue(this);

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter();
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
        rvFeedbacks.setAdapter(feedbackAdapter);
    }

    private void loadFeedbacks() {
        showLoading(true);

        String url = ApiConfig.GET_SHOP_FEEDBACKS_URL + "?shop_id=" + shopId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    showLoading(false);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("status")) {
                            JSONArray feedbacksArray = json.getJSONArray("feedbacks");
                            feedbackList.clear();

                            double totalRating = 0;
                            for (int i = 0; i < feedbacksArray.length(); i++) {
                                JSONObject obj = feedbacksArray.getJSONObject(i);
                                Feedback feedback = new Feedback();
                                feedback.setUserName(obj.optString("user_name", "Anonymous"));
                                feedback.setRating(obj.optInt("rating", 5));
                                feedback.setComments(obj.optString("comments", ""));
                                feedback.setCreatedAt(obj.optString("created_at", ""));
                                feedbackList.add(feedback);
                                totalRating += feedback.getRating();
                            }

                            // Update stats
                            int totalCount = feedbackList.size();
                            tvTotalReviews.setText(String.valueOf(totalCount));
                            if (totalCount > 0) {
                                double avg = totalRating / totalCount;
                                tvAvgRating.setText(String.format("⭐ %.1f", avg));
                            } else {
                                tvAvgRating.setText("⭐ 0.0");
                            }

                            feedbackAdapter.updateFeedbacks(feedbackList);
                            updateEmptyState();
                        } else {
                            updateEmptyState();
                        }
                    } catch (Exception e) {
                        updateEmptyState();
                    }
                },
                error -> {
                    showLoading(false);
                    updateEmptyState();
                }
        );

        requestQueue.add(request);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvFeedbacks.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (feedbackList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFeedbacks.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFeedbacks.setVisibility(View.VISIBLE);
        }
    }
}
