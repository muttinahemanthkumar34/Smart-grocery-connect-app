package com.SIMATS.Groceryconnect.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.SIMATS.Groceryconnect.models.AiMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton manager to persist AI chat messages per user.
 * Chat history is stored in SharedPreferences and persists across app restarts.
 */
public class AiChatManager {
    
    private static AiChatManager instance;
    private static final String PREF_NAME = "AiChatHistory";
    private static final String KEY_CHAT_PREFIX = "chat_user_";
    
    private List<AiMessage> chatHistory;
    private Context context;
    private int currentUserId = -1;
    
    private AiChatManager() {
        chatHistory = new ArrayList<>();
    }
    
    public static synchronized AiChatManager getInstance() {
        if (instance == null) {
            instance = new AiChatManager();
        }
        return instance;
    }
    
    /**
     * Initialize with context and user ID
     */
    public void init(Context context, int userId) {
        this.context = context.getApplicationContext();
        
        // If switching users, save current chat and load new user's chat
        if (currentUserId != userId) {
            if (currentUserId != -1) {
                saveToPrefs(); // Save previous user's chat
            }
            currentUserId = userId;
            loadFromPrefs(); // Load new user's chat
        }
    }
    
    /**
     * Get all chat messages for current user
     */
    public List<AiMessage> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }
    
    /**
     * Add a message to chat history and save
     */
    public void addMessage(AiMessage message) {
        chatHistory.add(message);
        saveToPrefs();
    }
    
    /**
     * Check if chat history is empty
     */
    public boolean isEmpty() {
        return chatHistory.isEmpty();
    }
    
    /**
     * Get the number of messages
     */
    public int getMessageCount() {
        return chatHistory.size();
    }
    
    /**
     * Clear chat history for current user (but keep in storage)
     * This is used when user wants to start fresh
     */
    public void clearHistory() {
        chatHistory.clear();
        if (context != null && currentUserId != -1) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_CHAT_PREFIX + currentUserId).apply();
        }
        currentUserId = -1;
    }
    
    /**
     * Save chat history to SharedPreferences
     */
    private void saveToPrefs() {
        if (context == null || currentUserId == -1) return;
        
        try {
            JSONArray jsonArray = new JSONArray();
            for (AiMessage msg : chatHistory) {
                JSONObject obj = new JSONObject();
                obj.put("type", msg.getType());
                obj.put("message", msg.getMessage());
                obj.put("isUser", msg.isUserMessage());
                obj.put("stepsShown", msg.isStepsShown());
                
                if (msg.getDishName() != null) obj.put("dishName", msg.getDishName());
                if (msg.getCategory() != null) obj.put("category", msg.getCategory());
                if (msg.getPrepTime() != null) obj.put("prepTime", msg.getPrepTime());
                
                if (msg.getIngredients() != null) {
                    JSONArray ingArr = new JSONArray();
                    for (String ing : msg.getIngredients()) ingArr.put(ing);
                    obj.put("ingredients", ingArr);
                }
                
                if (msg.getSteps() != null) {
                    JSONArray stepsArr = new JSONArray();
                    for (String step : msg.getSteps()) stepsArr.put(step);
                    obj.put("steps", stepsArr);
                }
                
                if (msg.getSuggestions() != null) {
                    JSONArray sugArr = new JSONArray();
                    for (String sug : msg.getSuggestions()) sugArr.put(sug);
                    obj.put("suggestions", sugArr);
                }
                
                jsonArray.put(obj);
            }
            
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_CHAT_PREFIX + currentUserId, jsonArray.toString()).apply();
            
        } catch (Exception e) {
            Log.e("AiChatManager", "Error saving chat history", e);
        }
    }
    
    /**
     * Load chat history from SharedPreferences
     */
    private void loadFromPrefs() {
        chatHistory.clear();
        
        if (context == null || currentUserId == -1) return;
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = prefs.getString(KEY_CHAT_PREFIX + currentUserId, null);
            
            if (jsonStr == null) return;
            
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                AiMessage msg = new AiMessage();
                msg.setType(obj.optString("type"));
                msg.setMessage(obj.optString("message"));
                msg.setUserMessage(obj.optBoolean("isUser"));
                msg.setStepsShown(obj.optBoolean("stepsShown"));
                msg.setNeedsAnimation(false); // Don't animate restored messages
                
                if (obj.has("dishName")) msg.setDishName(obj.getString("dishName"));
                if (obj.has("category")) msg.setCategory(obj.getString("category"));
                if (obj.has("prepTime")) msg.setPrepTime(obj.getString("prepTime"));
                
                if (obj.has("ingredients")) {
                    JSONArray ingArr = obj.getJSONArray("ingredients");
                    List<String> ingredients = new ArrayList<>();
                    for (int j = 0; j < ingArr.length(); j++) {
                        ingredients.add(ingArr.getString(j));
                    }
                    msg.setIngredients(ingredients);
                }
                
                if (obj.has("steps")) {
                    JSONArray stepsArr = obj.getJSONArray("steps");
                    List<String> steps = new ArrayList<>();
                    for (int j = 0; j < stepsArr.length(); j++) {
                        steps.add(stepsArr.getString(j));
                    }
                    msg.setSteps(steps);
                }
                
                if (obj.has("suggestions")) {
                    JSONArray sugArr = obj.getJSONArray("suggestions");
                    List<String> suggestions = new ArrayList<>();
                    for (int j = 0; j < sugArr.length(); j++) {
                        suggestions.add(sugArr.getString(j));
                    }
                    msg.setSuggestions(suggestions);
                }
                
                chatHistory.add(msg);
            }
            
        } catch (Exception e) {
            Log.e("AiChatManager", "Error loading chat history", e);
        }
    }
}
