package com.SIMATS.Groceryconnect.models;

import java.util.List;

/**
 * Model class representing a message in the AI Assistant chat.
 * Can be either a user message or an AI response.
 */
public class AiMessage {
    
    // Message types
    public static final String TYPE_USER = "user";
    public static final String TYPE_AI_TEXT = "ai_text";
    public static final String TYPE_AI_RECIPE = "ai_recipe";
    public static final String TYPE_AI_INGREDIENTS = "ai_ingredients";
    public static final String TYPE_AI_SUGGESTIONS = "ai_suggestions";
    public static final String TYPE_AI_MULTIPLE = "ai_multiple";
    
    private String type;
    private String message;
    private boolean isUserMessage;
    private boolean needsAnimation = false;
    private boolean stepsShown = false;
    private boolean needsStepsAnimation = false;
    
    // Recipe data (for AI recipe responses)
    private String dishName;
    private String category;
    private String prepTime;
    private String difficulty;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> suggestions; // For no-match responses
    private List<RecipeSummary> recipeList; // For multiple matches
    private int displayedSuggestionsCount = 10; // Track how many suggestions to show
    
    // Default constructor
    public AiMessage() {}
    
    // Constructor for user messages
    public AiMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.type = isUserMessage ? TYPE_USER : TYPE_AI_TEXT;
    }
    
    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isUserMessage() { return isUserMessage; }
    public void setUserMessage(boolean userMessage) { isUserMessage = userMessage; }
    
    public boolean isNeedsAnimation() { return needsAnimation; }
    public void setNeedsAnimation(boolean needsAnimation) { this.needsAnimation = needsAnimation; }
    
    public boolean isStepsShown() { return stepsShown; }
    public void setStepsShown(boolean stepsShown) { this.stepsShown = stepsShown; }
    
    public boolean isNeedsStepsAnimation() { return needsStepsAnimation; }
    public void setNeedsStepsAnimation(boolean needsStepsAnimation) { this.needsStepsAnimation = needsStepsAnimation; }
    
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getPrepTime() { return prepTime; }
    public void setPrepTime(String prepTime) { this.prepTime = prepTime; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    
    public List<RecipeSummary> getRecipeList() { return recipeList; }
    public void setRecipeList(List<RecipeSummary> recipeList) { this.recipeList = recipeList; }
    
    public int getDisplayedSuggestionsCount() { return displayedSuggestionsCount; }
    public void setDisplayedSuggestionsCount(int count) { this.displayedSuggestionsCount = count; }
    
    /**
     * Inner class for recipe summaries in multiple match responses
     */
    public static class RecipeSummary {
        private String dishName;
        private String category;
        private String prepTime;
        private String difficulty;
        
        public RecipeSummary(String dishName, String category, String prepTime) {
            this.dishName = dishName;
            this.category = category;
            this.prepTime = prepTime;
        }

        public RecipeSummary() {}
        
        public String getDishName() { return dishName; }
        public void setDishName(String dishName) { this.dishName = dishName; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getPrepTime() { return prepTime; }
        public void setPrepTime(String prepTime) { this.prepTime = prepTime; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    }
}
