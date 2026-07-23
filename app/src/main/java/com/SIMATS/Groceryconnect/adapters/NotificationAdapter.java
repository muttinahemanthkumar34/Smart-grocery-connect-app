package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private View unreadDot;
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTime;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            unreadDot = itemView.findViewById(R.id.view_unread_dot);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        void bind(Notification notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(notification.getTimeAgo());

            // Show/hide unread dot
            unreadDot.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Apply different styling for unread notifications
            if (!notification.isRead()) {
                tvTitle.setTextColor(0xFFFFFFFF); // White
                tvMessage.setTextColor(0xFFCCCCCC); // Light gray
                itemView.setBackgroundColor(0xFF1E1E1E); // Slightly lighter background
            } else {
                tvTitle.setTextColor(0xFFAAAAAA); // Gray
                tvMessage.setTextColor(0xFF777777); // Darker gray
                itemView.setBackgroundColor(0xFF121212); // Dark background
            }

            // Set icon color based on order status
            String status = notification.getOrderStatus();
            int iconTint = 0xFF10B981; // Default green
            if (status != null) {
                switch (status.toUpperCase()) {
                    case "CANCELLED":
                        iconTint = 0xFFFF4444; // Red
                        break;
                    case "DELIVERED":
                        iconTint = 0xFF10B981; // Green
                        break;
                    case "PACKING":
                    case "READY":
                        iconTint = 0xFFFF9800; // Orange
                        break;
                    default:
                        iconTint = 0xFF10B981; // Green
                }
            }
            ivIcon.setColorFilter(iconTint);
        }
    }
}
