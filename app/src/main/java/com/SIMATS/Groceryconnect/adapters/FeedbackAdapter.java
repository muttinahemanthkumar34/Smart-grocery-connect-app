package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Feedback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<Feedback> feedbackList = new ArrayList<>();

    public FeedbackAdapter() {}

    public void updateFeedbacks(List<Feedback> feedbacks) {
        this.feedbackList = feedbacks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvUserName, tvDate, tvRating, tvComment;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvComment = itemView.findViewById(R.id.tv_comment);
        }

        public void bind(Feedback feedback) {
            // Set user avatar (first letter of name)
            String userName = feedback.getUserName();
            if (userName != null && !userName.isEmpty()) {
                tvAvatar.setText(userName.substring(0, 1).toUpperCase());
                tvUserName.setText(userName);
            } else {
                tvAvatar.setText("U");
                tvUserName.setText("Anonymous User");
            }

            // Set rating stars
            tvRating.setText(feedback.getStarsDisplay());

            // Set date
            String createdAt = feedback.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                tvDate.setText(formatDate(createdAt));
            } else {
                tvDate.setText("");
            }

            // Set comment
            String comment = feedback.getComments();
            if (comment != null && !comment.isEmpty() && !comment.equals("null")) {
                tvComment.setVisibility(View.VISIBLE);
                tvComment.setText(comment);
            } else {
                tvComment.setVisibility(View.VISIBLE);
                tvComment.setText("No comment provided");
                tvComment.setTextColor(0xFF888888); // Lighter color for default message
            }
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // Try just date format
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(dateStr);
                    if (date != null) {
                        return outputFormat.format(date);
                    }
                } catch (ParseException e2) {
                    // Return as is
                }
            }
            return dateStr;
        }
    }
}
