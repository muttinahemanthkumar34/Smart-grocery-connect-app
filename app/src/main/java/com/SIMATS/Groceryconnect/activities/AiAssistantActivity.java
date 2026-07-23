package com.SIMATS.Groceryconnect.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.adapters.AiMessageAdapter;
import com.SIMATS.Groceryconnect.models.AiMessage;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.AiChatManager;
import com.SIMATS.Groceryconnect.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Grocery Assistant Activity
 * 
 * This activity implements a rule-based AI assistant that provides:
 * - Recipe ingredients (shown first)
 * - Recipe steps (shown on "Get Recipe" button click)
 * 
 * Features typing animation for chat-like experience.
 * Chat history is persisted using AiChatManager.
 */
public class AiAssistantActivity extends AppCompatActivity implements 
        AiMessageAdapter.OnSuggestionClickListener,
        AiMessageAdapter.OnGetRecipeClickListener,
        AiMessageAdapter.OnLoadMoreClickListener {

    private static final String TAG = "AiAssistant";

    private RecyclerView rvMessages;
    private EditText etQuery;
    private ImageView btnBack, btnSend, btnClearChat;
    private ProgressBar progressBar;
    private LinearLayout layoutThinking;
    private TextView tvThinking;
    private ImageView ivThinkingIcon;

    private AiMessageAdapter adapter;
    private RequestQueue requestQueue;
    private Handler typingHandler;
    
    // Thinking animation
    private Runnable thinkingRunnable;
    private int thinkingDotCount = 0;
    private Animation rotateAnimation;
    private long thinkingStartTime = 0;
    private static final long MIN_THINKING_TIME_MS = 1000; // Minimum 1 second thinking
    private String pendingResponse = null;
    private String pendingError = null;
    private AiChatManager chatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        // Initialize chat manager with current user
        SessionManager sessionManager = new SessionManager(this);
        chatManager = AiChatManager.getInstance();
        chatManager.init(this, sessionManager.getUserId());
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        requestQueue = Volley.newRequestQueue(this);
        typingHandler = new Handler(Looper.getMainLooper());

        // Load existing chat history or show welcome message
        loadChatHistory();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rv_messages);
        etQuery = findViewById(R.id.et_query);
        btnBack = findViewById(R.id.btn_back);
        btnSend = findViewById(R.id.btn_send);
        btnClearChat = findViewById(R.id.btn_clear_chat);
        progressBar = findViewById(R.id.progressBar);
        layoutThinking = findViewById(R.id.layout_thinking);
        tvThinking = findViewById(R.id.tv_thinking);
        ivThinkingIcon = findViewById(R.id.iv_thinking_icon);
        
        // Setup rotate animation for thinking icon
        rotateAnimation = new RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(1500);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
    }

    private void setupRecyclerView() {
        adapter = new AiMessageAdapter();
        adapter.setSuggestionClickListener(this);
        adapter.setGetRecipeClickListener(this);
        adapter.setLoadMoreClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendQuery());

        btnClearChat.setOnClickListener(v -> showClearChatConfirmation());

        // Handle keyboard send action
        etQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendQuery();
                return true;
            }
            return false;
        });
    }

    /**
     * Show confirmation dialog to clear chat
     */
    private void showClearChatConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Chat")
            .setMessage("Are you sure you want to clear the conversation? This cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> {
                // Clear chat history
                chatManager.clearHistory();
                adapter.clearMessages();
                
                // Reinitialize with user ID and show welcome
                SessionManager sessionManager = new SessionManager(this);
                chatManager.init(this, sessionManager.getUserId());
                showWelcomeMessage();
                
                Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Load chat history from ChatManager or show welcome message
     */
    private void loadChatHistory() {
        if (chatManager.isEmpty()) {
            // No history, show welcome message
            showWelcomeMessage();
        } else {
            // Restore previous chat history
            List<AiMessage> history = chatManager.getChatHistory();
            for (AiMessage message : history) {
                // Don't animate restored messages
                message.setNeedsAnimation(false);
                message.setNeedsStepsAnimation(false);
                adapter.addMessage(message);
            }
            scrollToBottom();
        }
    }

    private void showWelcomeMessage() {
        AiMessage welcome = new AiMessage();
        welcome.setUserMessage(false);
        welcome.setType(AiMessage.TYPE_AI_SUGGESTIONS);
        welcome.setMessage("👋 Hello! I'm your AI Grocery Assistant.\n\nAsk me about any dish and I'll help you with:\n• Ingredients list\n• Step-by-step recipe\n\nTry these popular dishes:");
        welcome.setNeedsAnimation(true);
        
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Chicken Biryani");
        suggestions.add("Spaghetti Pasta");
        suggestions.add("Paneer Butter Masala");
        welcome.setSuggestions(suggestions);
        
        adapter.addMessage(welcome);
        chatManager.addMessage(welcome);
    }

    private void sendQuery() {
        String query = etQuery.getText().toString().trim();
        
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Please enter a dish name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message
        AiMessage userMessage = new AiMessage(query, true);
        adapter.addMessage(userMessage);
        chatManager.addMessage(userMessage);
        scrollToBottom();

        // Clear input
        etQuery.setText("");

        // Show loading
        showLoading(true);

        // Send API request
        callAiAssistant(query);
    }

    private void callAiAssistant(String query) {
        try {
            // Create JSON body for FastAPI
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", query);
            
            com.android.volley.toolbox.JsonObjectRequest request = 
                new com.android.volley.toolbox.JsonObjectRequest(
                    Request.Method.POST, 
                    ApiConfig.AI_ASSISTANT_URL,
                    jsonBody,
                    response -> {
                        handleApiResponse(response.toString(), null);
                    },
                    error -> {
                        String errorMsg = "Sorry, I couldn't process your request. Please try again.";
                        if (error.networkResponse != null) {
                            Log.e(TAG, "AI Error code: " + error.networkResponse.statusCode);
                            Log.e(TAG, "AI Error data: " + new String(error.networkResponse.data));
                        } else if (error.getMessage() != null) {
                            Log.e(TAG, "AI Error: " + error.getMessage());
                        }
                        handleApiResponse(null, errorMsg);
                    }
                );
            
            // Set timeout for AI requests (may take longer)
            request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                30000, // 30 second timeout
                0,     // no retries
                1.0f   // no backoff
            ));
            
            requestQueue.add(request);
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
            showLoading(false);
            showErrorMessage("Error sending request. Please try again.");
        }
    }

    private void parseAndDisplayResponse(String response) {
        try {
            Log.d(TAG, "Raw response: " + response);
            JSONObject json = new JSONObject(response);
            
            // Use optBoolean with default true to handle different status formats
            boolean status = json.optBoolean("status", true);
            if (!status) {
                // Error response
                String message = json.optString("message", "Something went wrong");
                showErrorMessage(message);
                return;
            }

            String type = json.optString("type", "");
            String message = json.optString("message", "");
            
            AiMessage aiMessage = new AiMessage();
            aiMessage.setUserMessage(false);
            aiMessage.setMessage(message);
            aiMessage.setNeedsAnimation(true);

            switch (type) {
                case "recipe":
                    // Check if we have valid recipe data with ingredients
                    JSONObject data = json.optJSONObject("data");
                    JSONArray ingredientsArray = (data != null) ? data.optJSONArray("ingredients") : null;
                    
                    // If no ingredients, show as text response instead of empty recipe card
                    if (ingredientsArray == null || ingredientsArray.length() == 0) {
                        aiMessage.setType(AiMessage.TYPE_AI_TEXT);
                        Log.d(TAG, "Recipe has no ingredients, showing as text");
                        break;
                    }
                    
                    // Single recipe response - show ingredients first
                    aiMessage.setType(AiMessage.TYPE_AI_INGREDIENTS);
                    
                    aiMessage.setDishName(data.optString("dish_name", "Recipe"));
                    aiMessage.setCategory(data.optString("category", "General"));
                    aiMessage.setPrepTime(data.optString("prep_time", "30 mins"));
                    aiMessage.setDifficulty(data.optString("difficulty", "Easy"));
                    
                    // Parse ingredients
                    List<String> ingredients = new ArrayList<>();
                    for (int i = 0; i < ingredientsArray.length(); i++) {
                        String ingredient = ingredientsArray.optString(i, "").trim();
                        if (!ingredient.isEmpty()) {
                            ingredients.add(ingredient);
                        }
                    }
                    aiMessage.setIngredients(ingredients);
                    
                    // Parse steps (but don't show yet)
                    JSONArray stepsArray = data.optJSONArray("steps");
                    if (stepsArray != null && stepsArray.length() > 0) {
                        List<String> steps = new ArrayList<>();
                        for (int i = 0; i < stepsArray.length(); i++) {
                            String step = stepsArray.optString(i, "").trim();
                            if (!step.isEmpty()) {
                                steps.add(step);
                            }
                        }
                        aiMessage.setSteps(steps);
                    }
                    aiMessage.setStepsShown(false); // Will show when user clicks Get Recipe
                    break;

                case "no_match":
                    // No recipe found - show suggestions
                    aiMessage.setType(AiMessage.TYPE_AI_SUGGESTIONS);
                    
                    JSONArray suggestionsArray = json.optJSONArray("suggestions");
                    if (suggestionsArray != null) {
                        List<String> suggestions = new ArrayList<>();
                        for (int i = 0; i < suggestionsArray.length(); i++) {
                            suggestions.add(suggestionsArray.getString(i));
                        }
                        aiMessage.setSuggestions(suggestions);
                    }
                    break;

                case "multiple_matches":
                    // Multiple recipes found
                    aiMessage.setType(AiMessage.TYPE_AI_MULTIPLE);
                    
                    // Try to get recipes array
                    JSONArray recipesArray = json.optJSONArray("recipes");
                    if (recipesArray == null) {
                        recipesArray = json.optJSONArray("items"); // Fallback key
                    }
                    
                    if (recipesArray != null) {
                        List<AiMessage.RecipeSummary> summaryList = new ArrayList<>();
                        List<String> recipeNames = new ArrayList<>();
                        
                        for (int i = 0; i < recipesArray.length(); i++) {
                            JSONObject recipeObj = recipesArray.getJSONObject(i);
                            
                            // Extract details
                            String name = recipeObj.optString("dish_name", recipeObj.optString("name", "Unknown Dish"));
                            String cat = recipeObj.optString("category", "General");
                            String time = recipeObj.optString("prep_time", "N/A");
                            String diff = recipeObj.optString("difficulty", "Medium");
                            
                            AiMessage.RecipeSummary summary = new AiMessage.RecipeSummary(name, cat, time);
                            summary.setDifficulty(diff);
                            summaryList.add(summary);
                            
                            recipeNames.add(name);
                        }
                        aiMessage.setRecipeList(summaryList);
                        aiMessage.setSuggestions(recipeNames); // Keep as fallback/chips if needed
                    }
                    break;

                case "greeting":
                case "help":
                    // Greeting or help response with suggestions
                    aiMessage.setType(AiMessage.TYPE_AI_SUGGESTIONS);
                    
                    JSONArray greetSuggestions = json.optJSONArray("suggestions");
                    if (greetSuggestions != null) {
                        List<String> suggestions = new ArrayList<>();
                        for (int i = 0; i < greetSuggestions.length(); i++) {
                            suggestions.add(greetSuggestions.getString(i));
                        }
                        aiMessage.setSuggestions(suggestions);
                    } else {
                        // No suggestions, show as plain text
                        aiMessage.setType(AiMessage.TYPE_AI_TEXT);
                    }
                    break;

                case "suggestion":
                case "veg_items":
                case "non_veg_items":
                case "items":
                    // Food suggestions with various possible array formats
                    aiMessage.setType(AiMessage.TYPE_AI_SUGGESTIONS);
                    
                    List<String> foodSuggestions = new ArrayList<>();
                    
                    // Try data object first
                    JSONObject suggestionData = json.optJSONObject("data");
                    if (suggestionData != null) {
                        // Get non-veg items
                        JSONArray nonVegArray = suggestionData.optJSONArray("non_veg");
                        if (nonVegArray != null) {
                            for (int i = 0; i < nonVegArray.length(); i++) {
                                String item = nonVegArray.optString(i, "").trim();
                                if (!item.isEmpty() && !foodSuggestions.contains(item)) {
                                    foodSuggestions.add(item);
                                }
                            }
                        }
                        
                        // Get veg items
                        JSONArray vegArray = suggestionData.optJSONArray("veg");
                        if (vegArray != null) {
                            for (int i = 0; i < vegArray.length(); i++) {
                                String item = vegArray.optString(i, "").trim();
                                if (!item.isEmpty() && !foodSuggestions.contains(item)) {
                                    foodSuggestions.add(item);
                                }
                            }
                        }
                        
                        // Check for generic items array in data
                        JSONArray dataItems = suggestionData.optJSONArray("items");
                        if (dataItems != null) {
                            for (int i = 0; i < dataItems.length(); i++) {
                                String item = dataItems.optString(i, "").trim();
                                if (!item.isEmpty() && !foodSuggestions.contains(item)) {
                                    foodSuggestions.add(item);
                                }
                            }
                        }
                    }
                    
                    // Fallback: check for items array at root level
                    if (foodSuggestions.isEmpty()) {
                        JSONArray itemsArray = json.optJSONArray("items");
                        if (itemsArray != null) {
                            for (int i = 0; i < itemsArray.length(); i++) {
                                String item = itemsArray.optString(i, "").trim();
                                if (!item.isEmpty() && !foodSuggestions.contains(item)) {
                                    foodSuggestions.add(item);
                                }
                            }
                        }
                    }
                    
                    // Fallback: check for suggestions array at root level
                    if (foodSuggestions.isEmpty()) {
                        JSONArray sugArray = json.optJSONArray("suggestions");
                        if (sugArray != null) {
                            for (int i = 0; i < sugArray.length(); i++) {
                                String item = sugArray.optString(i, "").trim();
                                if (!item.isEmpty() && !foodSuggestions.contains(item)) {
                                    foodSuggestions.add(item);
                                }
                            }
                        }
                    }
                    
                    if (!foodSuggestions.isEmpty()) {
                        aiMessage.setSuggestions(foodSuggestions);
                    } else {
                        aiMessage.setType(AiMessage.TYPE_AI_TEXT);
                    }
                    break;

                case "text":
                case "info":
                case "error":
                default:
                    // Generic text response - no recipe card
                    aiMessage.setType(AiMessage.TYPE_AI_TEXT);
                    break;
            }

            adapter.addMessage(aiMessage);
            chatManager.addMessage(aiMessage);
            scrollToBottom();

        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            showErrorMessage("Error parsing response. Please try again.");
        }
    }

    private void showErrorMessage(String message) {
        AiMessage errorMessage = new AiMessage();
        errorMessage.setUserMessage(false);
        errorMessage.setType(AiMessage.TYPE_AI_TEXT);
        errorMessage.setMessage("❌ " + message);
        errorMessage.setNeedsAnimation(true);
        
        adapter.addMessage(errorMessage);
        chatManager.addMessage(errorMessage);
        scrollToBottom();
    }

    private void showLoading(boolean show) {
        if (show) {
            startThinkingAnimation();
        } else {
            stopThinkingAnimation();
        }
        btnSend.setEnabled(!show);
    }
    
    /**
     * Handle API response with minimum thinking time
     */
    private void handleApiResponse(String response, String error) {
        long elapsedTime = System.currentTimeMillis() - thinkingStartTime;
        long remainingTime = MIN_THINKING_TIME_MS - elapsedTime;
        
        if (remainingTime > 0) {
            // Need to wait a bit more before showing response
            pendingResponse = response;
            pendingError = error;
            typingHandler.postDelayed(() -> {
                showLoading(false);
                processPendingResponse();
            }, remainingTime);
        } else {
            // Already waited enough, show immediately
            showLoading(false);
            if (error != null) {
                showErrorMessage(error);
            } else if (response != null) {
                Log.d(TAG, "AI Response: " + response);
                parseAndDisplayResponse(response);
            }
        }
    }
    
    private void processPendingResponse() {
        if (pendingError != null) {
            showErrorMessage(pendingError);
            pendingError = null;
        } else if (pendingResponse != null) {
            Log.d(TAG, "AI Response: " + pendingResponse);
            parseAndDisplayResponse(pendingResponse);
            pendingResponse = null;
        }
    }
    
    private void startThinkingAnimation() {
        layoutThinking.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        
        // Track when thinking started
        thinkingStartTime = System.currentTimeMillis();
        
        // Start icon rotation
        ivThinkingIcon.startAnimation(rotateAnimation);
        
        // Animate dots
        thinkingDotCount = 0;
        thinkingRunnable = new Runnable() {
            @Override
            public void run() {
                thinkingDotCount = (thinkingDotCount % 3) + 1;
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < thinkingDotCount; i++) {
                    dots.append(".");
                }
                tvThinking.setText("AI is thinking" + dots.toString());
                typingHandler.postDelayed(this, 400);
            }
        };
        typingHandler.post(thinkingRunnable);
    }
    
    private void stopThinkingAnimation() {
        layoutThinking.setVisibility(View.GONE);
        ivThinkingIcon.clearAnimation();
        
        if (thinkingRunnable != null) {
            typingHandler.removeCallbacks(thinkingRunnable);
            thinkingRunnable = null;
        }
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.postDelayed(() -> {
                rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
            }, 100);
        }
    }

    @Override
    public void onSuggestionClick(String suggestion) {
        // Auto-fill and send the suggestion
        etQuery.setText(suggestion);
        sendQuery();
    }

    @Override
    public void onGetRecipeClick(AiMessage message, int position) {
        // Mark steps as shown with animation and update the adapter
        message.setStepsShown(true);
        message.setNeedsStepsAnimation(true);
        adapter.updateMessage(position);
        
        // Scroll to show the steps
        typingHandler.postDelayed(() -> scrollToBottom(), 300);
    }

    @Override
    public void onLoadMoreClick(AiMessage message, int position) {
        // Increase the displayed count by 10
        int currentCount = message.getDisplayedSuggestionsCount();
        int totalSuggestions = message.getSuggestions() != null ? message.getSuggestions().size() : 0;
        
        // Add 10 more or show all remaining
        int newCount = Math.min(currentCount + 10, totalSuggestions);
        message.setDisplayedSuggestionsCount(newCount);
        
        // Update the adapter to refresh the display
        adapter.updateMessage(position);
        
        // Scroll to show the new items
        typingHandler.postDelayed(() -> scrollToBottom(), 100);
    }
}
