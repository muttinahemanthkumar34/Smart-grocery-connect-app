package com.SIMATS.Groceryconnect.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.AiMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying AI chat messages in RecyclerView.
 * Supports typing animation and dynamic suggestion display.
 */
public class AiMessageAdapter extends RecyclerView.Adapter<AiMessageAdapter.MessageViewHolder> {
    
    private List<AiMessage> messages = new ArrayList<>();
    private OnSuggestionClickListener suggestionClickListener;
    private OnGetRecipeClickListener getRecipeClickListener;
    
    // Typing animation settings
    private static final int TYPING_DELAY_MS = 8;
    
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    
    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }
    
    public interface OnGetRecipeClickListener {
        void onGetRecipeClick(AiMessage message, int position);
    }
    
    public interface OnLoadMoreClickListener {
        void onLoadMoreClick(AiMessage message, int position);
    }
    
    public void setSuggestionClickListener(OnSuggestionClickListener listener) {
        this.suggestionClickListener = listener;
    }
    
    public void setGetRecipeClickListener(OnGetRecipeClickListener listener) {
        this.getRecipeClickListener = listener;
    }
    
    private OnLoadMoreClickListener loadMoreClickListener;
    
    public void setLoadMoreClickListener(OnLoadMoreClickListener listener) {
        this.loadMoreClickListener = listener;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        AiMessage message = messages.get(position);
        holder.bind(message, position);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void addMessage(AiMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void updateMessage(int position) {
        if (position >= 0 && position < messages.size()) {
            notifyItemChanged(position);
        }
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    private void animateText(TextView textView, String fullText, Runnable onComplete) {
        if (fullText == null || fullText.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        textView.setText("");
        final int[] currentIndex = {0};
        
        Runnable typeWriter = new Runnable() {
            @Override
            public void run() {
                if (currentIndex[0] <= fullText.length()) {
                    textView.setText(fullText.substring(0, currentIndex[0]));
                    currentIndex[0]++;
                    animationHandler.postDelayed(this, TYPING_DELAY_MS);
                } else {
                    if (onComplete != null) onComplete.run();
                }
            }
        };
        
        animationHandler.post(typeWriter);
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
        
        private LinearLayout layoutUserMessage, layoutAiMessage;
        private TextView tvUserMessage, tvAiMessage;
        private LinearLayout layoutIngredientsCard, layoutRecipeSteps;
        private LinearLayout layoutSuggestionsCard;
        private FlexboxLayout layoutSuggestions;
        private TextView tvRecipeName, tvCategory, tvPrepTime, tvSuggestionsHeader;
        private TextView tvIngredients, tvSteps;
        private Button btnGetRecipe;
        private LinearLayout layoutRecipeList;
        private Button btnLoadMore;
        private Context context;
        
        // For tracking displayed suggestions vs total
        private static final int MAX_INITIAL_SUGGESTIONS = 10;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            
            layoutUserMessage = itemView.findViewById(R.id.layout_user_message);
            layoutAiMessage = itemView.findViewById(R.id.layout_ai_message);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
            layoutIngredientsCard = itemView.findViewById(R.id.layout_ingredients_card);
            layoutRecipeSteps = itemView.findViewById(R.id.layout_recipe_steps);
            layoutSuggestionsCard = itemView.findViewById(R.id.layout_suggestions_card);
            layoutSuggestions = itemView.findViewById(R.id.layout_suggestions);
            tvSuggestionsHeader = itemView.findViewById(R.id.tv_suggestions_header);
            tvRecipeName = itemView.findViewById(R.id.tv_recipe_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrepTime = itemView.findViewById(R.id.tv_prep_time);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients);
            tvSteps = itemView.findViewById(R.id.tv_steps);
            btnGetRecipe = itemView.findViewById(R.id.btn_get_recipe);
            layoutRecipeList = itemView.findViewById(R.id.layout_recipe_list);
            btnLoadMore = itemView.findViewById(R.id.btn_load_more);
        }
        
        public void bind(AiMessage message, int position) {
            // Reset visibility
            layoutUserMessage.setVisibility(View.GONE);
            layoutAiMessage.setVisibility(View.GONE);
            layoutIngredientsCard.setVisibility(View.GONE);
            layoutRecipeSteps.setVisibility(View.GONE);
            layoutSuggestionsCard.setVisibility(View.GONE);
            btnGetRecipe.setVisibility(View.GONE);
            layoutRecipeList.setVisibility(View.GONE);
            btnLoadMore.setVisibility(View.GONE);
            layoutSuggestions.removeAllViews();
            layoutRecipeList.removeAllViews();
            
            if (message.isUserMessage()) {
                layoutUserMessage.setVisibility(View.VISIBLE);
                tvUserMessage.setText(message.getMessage());
            } else {
                layoutAiMessage.setVisibility(View.VISIBLE);
                
                if (message.isNeedsAnimation()) {
                    animateAIResponse(message, position);
                } else {
                    showWithoutAnimation(message, position);
                }
            }
        }
        
        private void animateAIResponse(AiMessage message, int position) {
            String type = message.getType();
            
            tvAiMessage.setVisibility(View.VISIBLE);
            
            // For recipe types, show ingredients card and button immediately
            // This prevents issues with view recycling during text animation
            if (AiMessage.TYPE_AI_RECIPE.equals(type) || AiMessage.TYPE_AI_INGREDIENTS.equals(type)) {
                layoutIngredientsCard.setVisibility(View.VISIBLE);
                tvRecipeName.setText(message.getDishName());
                tvCategory.setText(message.getCategory());
                
                // Show button immediately if steps not shown
                if (!message.isStepsShown()) {
                    btnGetRecipe.setVisibility(View.VISIBLE);
                    btnGetRecipe.setOnClickListener(v -> {
                        if (getRecipeClickListener != null) {
                            getRecipeClickListener.onGetRecipeClick(message, position);
                        }
                    });
                }
            }
            
            animateText(tvAiMessage, message.getMessage(), () -> {
                message.setNeedsAnimation(false);
                
                if (AiMessage.TYPE_AI_RECIPE.equals(type) || AiMessage.TYPE_AI_INGREDIENTS.equals(type)) {
                    // Card is already visible, just animate ingredients
                    animateIngredientsOnly(message, position);
                } else if (AiMessage.TYPE_AI_SUGGESTIONS.equals(type) || AiMessage.TYPE_AI_MULTIPLE.equals(type)) {
                    animateSuggestions(message, position);
                }
            });
        }
        
        /**
         * Animate just the ingredients text (card is already visible)
         */
        private void animateIngredientsOnly(AiMessage message, int position) {
            tvIngredients.setText("");
            if (message.getIngredients() != null && !message.getIngredients().isEmpty()) {
                animateIngredientAtIndex(message.getIngredients(), 0, new StringBuilder(), () -> {
                    if (message.isStepsShown()) {
                        animateStepsCard(message);
                    }
                });
            } else if (message.isStepsShown()) {
                animateStepsCard(message);
            }
        }
        
        private void animateIngredientsCard(AiMessage message, int position) {
            layoutIngredientsCard.setVisibility(View.VISIBLE);
            
            tvRecipeName.setText(message.getDishName());
            tvCategory.setText(message.getCategory());
            
            // IMPORTANT: Show button immediately to avoid view recycling issues
            // Button is only hidden if steps have already been shown
            if (!message.isStepsShown()) {
                btnGetRecipe.setVisibility(View.VISIBLE);
                btnGetRecipe.setOnClickListener(v -> {
                    if (getRecipeClickListener != null) {
                        getRecipeClickListener.onGetRecipeClick(message, position);
                    }
                });
            } else {
                btnGetRecipe.setVisibility(View.GONE);
            }
            
            // Initialize ingredients text and animate ingredient by ingredient
            tvIngredients.setText("");
            if (message.getIngredients() != null && !message.getIngredients().isEmpty()) {
                animateIngredientAtIndex(message.getIngredients(), 0, new StringBuilder(), () -> {
                    // Animation complete - if steps were already shown, animate them
                    if (message.isStepsShown()) {
                        animateStepsCard(message);
                    }
                });
            } else if (message.isStepsShown()) {
                // No ingredients but steps shown, animate steps
                animateStepsCard(message);
            }
        }
        
        /**
         * Animate each ingredient one by one with typing animation
         */
        private void animateIngredientAtIndex(List<String> ingredients, int index, StringBuilder accumulatedText, Runnable onComplete) {
            if (index >= ingredients.size()) {
                if (onComplete != null) onComplete.run();
                return;
            }
            
            String ingredient = "• " + ingredients.get(index) + (index < ingredients.size() - 1 ? "\n" : "");
            
            // Show previously animated ingredients immediately
            tvIngredients.setText(accumulatedText.toString());
            
            // Animate current ingredient character by character
            final int[] currentChar = {0};
            Runnable ingredientAnimator = new Runnable() {
                @Override
                public void run() {
                    if (currentChar[0] <= ingredient.length()) {
                        tvIngredients.setText(accumulatedText.toString() + ingredient.substring(0, currentChar[0]));
                        currentChar[0]++;
                        animationHandler.postDelayed(this, TYPING_DELAY_MS);
                    } else {
                        // Ingredient complete, add to accumulated and animate next
                        accumulatedText.append(ingredient);
                        animateIngredientAtIndex(ingredients, index + 1, accumulatedText, onComplete);
                    }
                }
            };
            animationHandler.post(ingredientAnimator);
        }
        
        private void animateStepsCard(AiMessage message) {
            layoutRecipeSteps.setVisibility(View.VISIBLE);
            btnGetRecipe.setVisibility(View.GONE); // Hide button after clicking
            tvPrepTime.setText("⏱ " + (message.getPrepTime() != null ? message.getPrepTime() : "30 mins"));
            
            // Initialize steps text and animate step by step
            tvSteps.setText("");
            if (message.getSteps() != null && !message.getSteps().isEmpty()) {
                animateStepAtIndex(message.getSteps(), 0, new StringBuilder());
            }
        }
        
        /**
         * Animate each step one by one with typing animation
         */
        private void animateStepAtIndex(List<String> steps, int index, StringBuilder accumulatedText) {
            if (index >= steps.size()) return;
            
            String step = steps.get(index);
            String stepWithNewlines = step + (index < steps.size() - 1 ? "\n\n" : "");
            
            // Show previously animated steps immediately
            tvSteps.setText(accumulatedText.toString());
            
            // Animate current step character by character
            final int[] currentChar = {0};
            Runnable stepAnimator = new Runnable() {
                @Override
                public void run() {
                    if (currentChar[0] <= stepWithNewlines.length()) {
                        tvSteps.setText(accumulatedText.toString() + stepWithNewlines.substring(0, currentChar[0]));
                        currentChar[0]++;
                        animationHandler.postDelayed(this, TYPING_DELAY_MS);
                    } else {
                        // Step complete, add to accumulated and animate next
                        accumulatedText.append(stepWithNewlines);
                        animateStepAtIndex(steps, index + 1, accumulatedText);
                    }
                }
            };
            animationHandler.post(stepAnimator);
        }
        
        private void animateSuggestions(AiMessage message, int position) {
            List<String> suggestions = message.getSuggestions();
            if (suggestions != null && !suggestions.isEmpty()) {
                layoutSuggestionsCard.setVisibility(View.VISIBLE);
                layoutSuggestions.removeAllViews();
                
                // Use message's displayedCount for how many to show
                int displayedCount = message.getDisplayedSuggestionsCount();
                int maxToShow = Math.min(suggestions.size(), displayedCount);
                List<String> visibleSuggestions = suggestions.subList(0, maxToShow);
                
                // Animate suggestions one by one
                animateSuggestionAtIndex(visibleSuggestions, 0, message, position);
            }
        }
        
        private void animateSuggestionAtIndex(List<String> suggestions, int index, AiMessage message, int position) {
            if (index >= suggestions.size()) {
                // Done animating - show Load More if there are more items
                List<String> allSuggestions = message.getSuggestions();
                int displayedCount = message.getDisplayedSuggestionsCount();
                
                if (allSuggestions != null && allSuggestions.size() > displayedCount) {
                    int remaining = allSuggestions.size() - displayedCount;
                    btnLoadMore.setVisibility(View.VISIBLE);
                    btnLoadMore.setText("Load More (" + remaining + " more)");
                    btnLoadMore.setOnClickListener(v -> {
                        if (loadMoreClickListener != null) {
                            loadMoreClickListener.onLoadMoreClick(message, position);
                        }
                    });
                } else {
                    btnLoadMore.setVisibility(View.GONE);
                }
                return;
            }
            
            String suggestion = suggestions.get(index);
            TextView tv = createSuggestionView(suggestion);
            layoutSuggestions.addView(tv);
            
            // Animate text
            String fullText = suggestion;
            animateText(tv, fullText, () -> {
                // Setup click listener
                tv.setOnClickListener(v -> {
                    if (suggestionClickListener != null) {
                        suggestionClickListener.onSuggestionClick(suggestion);
                    }
                });
                // Animate next suggestion
                animateSuggestionAtIndex(suggestions, index + 1, message, position);
            });
        }
        
        private TextView createSuggestionView(String text) {
            TextView tv = new TextView(context);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, dpToPx(4), dpToPx(6), dpToPx(4));
            tv.setLayoutParams(params);
            tv.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
            tv.setBackgroundResource(R.drawable.bg_chip_unselected);
            tv.setTextColor(Color.parseColor("#10B981"));
            tv.setTextSize(13);
            tv.setText(text);
            return tv;
        }
        
        private void showWithoutAnimation(AiMessage message, int position) {
            tvAiMessage.setText(message.getMessage());
            tvAiMessage.setVisibility(View.VISIBLE);
            
            String type = message.getType();
            
            if (AiMessage.TYPE_AI_RECIPE.equals(type) || AiMessage.TYPE_AI_INGREDIENTS.equals(type)) {
                layoutIngredientsCard.setVisibility(View.VISIBLE);
                
                tvRecipeName.setText(message.getDishName());
                tvCategory.setText(message.getCategory());
                
                if (message.getIngredients() != null) {
                    StringBuilder ingredients = new StringBuilder();
                    for (String ingredient : message.getIngredients()) {
                        ingredients.append("• ").append(ingredient).append("\n");
                    }
                    tvIngredients.setText(ingredients.toString().trim());
                }
                
                if (message.isStepsShown()) {
                    btnGetRecipe.setVisibility(View.GONE);
                    
                    if (message.isNeedsStepsAnimation()) {
                        message.setNeedsStepsAnimation(false);
                        animateStepsCard(message);
                    } else {
                        layoutRecipeSteps.setVisibility(View.VISIBLE);
                        tvPrepTime.setText("⏱ " + (message.getPrepTime() != null ? message.getPrepTime() : "30 mins"));
                        
                        if (message.getSteps() != null) {
                            StringBuilder steps = new StringBuilder();
                            for (int i = 0; i < message.getSteps().size(); i++) {
                                steps.append(message.getSteps().get(i)).append("\n\n");
                            }
                            tvSteps.setText(steps.toString().trim());
                        }
                    }
                } else {
                    // Steps not shown yet - display the Get Recipe button
                    btnGetRecipe.setVisibility(View.VISIBLE);
                    final int pos = position;
                    btnGetRecipe.setOnClickListener(v -> {
                        if (getRecipeClickListener != null) {
                            getRecipeClickListener.onGetRecipeClick(message, pos);
                        }
                    });
                }
                
            } else if (AiMessage.TYPE_AI_MULTIPLE.equals(type)) {
                // Handle multiple matches with detailed cards
                layoutRecipeList.setVisibility(View.VISIBLE);
                layoutRecipeList.removeAllViews();
                
                List<AiMessage.RecipeSummary> recipes = message.getRecipeList();
                if (recipes != null && !recipes.isEmpty()) {
                    // Render detailed cards
                    for (AiMessage.RecipeSummary recipe : recipes) {
                        addRecipeSummaryCard(recipe, position);
                    }
                    
                    // Show Load More button
                    btnLoadMore.setVisibility(View.VISIBLE);
                    btnLoadMore.setOnClickListener(v -> {
                        if (suggestionClickListener != null) {
                            suggestionClickListener.onSuggestionClick("Show more items like this");
                        }
                    });
                } else {
                    // Fallback to chips if no detailed list
                    List<String> suggestions = message.getSuggestions();
                    if (suggestions != null && !suggestions.isEmpty()) {
                        layoutSuggestionsCard.setVisibility(View.VISIBLE);
                        layoutSuggestions.removeAllViews();
                        
                        int maxToShow = Math.min(suggestions.size(), MAX_INITIAL_SUGGESTIONS);
                        for (int i = 0; i < maxToShow; i++) {
                            String suggestion = suggestions.get(i);
                            TextView tv = createSuggestionView(suggestion);
                            final String suggestionText = suggestion;
                            tv.setOnClickListener(v -> {
                                if (suggestionClickListener != null) {
                                    suggestionClickListener.onSuggestionClick(suggestionText);
                                }
                            });
                            layoutSuggestions.addView(tv);
                        }
                        
                        // Show Load More if needed
                        if (suggestions.size() > MAX_INITIAL_SUGGESTIONS) {
                            btnLoadMore.setVisibility(View.VISIBLE);
                            btnLoadMore.setText("↻ Load More (" + (suggestions.size() - MAX_INITIAL_SUGGESTIONS) + " more)");
                        }
                    }
                }
            } else if (AiMessage.TYPE_AI_SUGGESTIONS.equals(type)) {
                List<String> suggestions = message.getSuggestions();
                if (suggestions != null && !suggestions.isEmpty()) {
                    layoutSuggestionsCard.setVisibility(View.VISIBLE);
                    layoutSuggestions.removeAllViews();
                    
                    // Use message's displayedCount for pagination
                    int displayedCount = message.getDisplayedSuggestionsCount();
                    int maxToShow = Math.min(suggestions.size(), displayedCount);
                    
                    for (int i = 0; i < maxToShow; i++) {
                        String suggestion = suggestions.get(i);
                        TextView tv = createSuggestionView(suggestion);
                        final String suggestionText = suggestion;
                        tv.setOnClickListener(v -> {
                            if (suggestionClickListener != null) {
                                suggestionClickListener.onSuggestionClick(suggestionText);
                            }
                        });
                        layoutSuggestions.addView(tv);
                    }
                    
                    // Show Load More button if there are more items
                    if (suggestions.size() > displayedCount) {
                        int remaining = suggestions.size() - displayedCount;
                        btnLoadMore.setVisibility(View.VISIBLE);
                        btnLoadMore.setText("Load More (" + remaining + " more)");
                        final int pos = position;
                        btnLoadMore.setOnClickListener(v -> {
                            if (loadMoreClickListener != null) {
                                loadMoreClickListener.onLoadMoreClick(message, pos);
                            }
                        });
                    } else {
                        btnLoadMore.setVisibility(View.GONE);
                    }
                }
            }
        }
        
        private void addRecipeSummaryCard(AiMessage.RecipeSummary recipe, int position) {
            View cardView = LayoutInflater.from(context).inflate(R.layout.item_ai_recipe_summary, layoutRecipeList, false);
            
            TextView tvName = cardView.findViewById(R.id.tv_summary_dish_name);
            TextView tvTime = cardView.findViewById(R.id.tv_summary_prep_time);
            TextView tvCategory = cardView.findViewById(R.id.tv_summary_category);
            Button btnView = cardView.findViewById(R.id.btn_view_recipe);
            
            tvName.setText(recipe.getDishName());
            tvTime.setText("⏱ " + recipe.getPrepTime());
            tvCategory.setText(recipe.getCategory());
            
            btnView.setOnClickListener(v -> {
                if (suggestionClickListener != null) {
                    suggestionClickListener.onSuggestionClick(recipe.getDishName());
                }
            });
            
            layoutRecipeList.addView(cardView);
        }

        private int dpToPx(int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
