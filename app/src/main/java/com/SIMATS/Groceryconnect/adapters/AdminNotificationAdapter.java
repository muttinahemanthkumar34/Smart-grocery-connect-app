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
 * Adapter for admin notifications with orange-themed UI
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public AdminNotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void markAllAsRead() {
        for (Notification n : notifications) {
            n.setRead(true);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View viewUnreadIndicator;
        private final ImageView ivNotificationIcon;
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
            ivNotificationIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
        }

        void bind(Notification notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(notification.getTimeAgo());

            // Show/hide unread indicator
            viewUnreadIndicator.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

            // Style based on read status
            if (notification.isRead()) {
                tvTitle.setAlpha(0.7f);
                tvMessage.setAlpha(0.6f);
            } else {
                tvTitle.setAlpha(1.0f);
                tvMessage.setAlpha(0.85f);
            }

            // Set icon based on notification type
            String title = notification.getTitle().toLowerCase();
            if (title.contains("cancel")) {
                ivNotificationIcon.setImageResource(R.drawable.ic_close);
                ivNotificationIcon.setColorFilter(0xFFFF5252); // Red for cancelled
            } else if (title.contains("new order") || title.contains("received")) {
                ivNotificationIcon.setImageResource(R.drawable.ic_orders);
                ivNotificationIcon.setColorFilter(0xFF10B981); // Green for new orders
            } else {
                ivNotificationIcon.setImageResource(R.drawable.ic_notification);
                ivNotificationIcon.setColorFilter(0xFFFF8C00); // Orange default
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}
