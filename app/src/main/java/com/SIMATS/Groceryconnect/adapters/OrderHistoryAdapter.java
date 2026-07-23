package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying order history items.
 */
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private List<Order> orderListFull; // For filtering
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onViewDetails(Order order);
        void onCancelOrder(Order order);
        void onSubmitFeedback(Order order);
    }

    public OrderHistoryAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = new ArrayList<>(orderList);
        this.orderListFull = new ArrayList<>(orderList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    /**
     * Update the order list with new data.
     */
    public void updateOrders(List<Order> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        this.orderListFull.clear();
        this.orderListFull.addAll(newOrders);
        notifyDataSetChanged();
    }

    /**
     * Filter orders by search query (shop name or date).
     */
    public void filter(String query) {
        orderList.clear();
        if (query.isEmpty()) {
            orderList.addAll(orderListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Order order : orderListFull) {
                if (order.getShopName().toLowerCase().contains(lowerQuery) ||
                    order.getFormattedDate().toLowerCase().contains(lowerQuery) ||
                    order.getCreatedAt().toLowerCase().contains(lowerQuery)) {
                    orderList.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Update a specific order's status after cancellation.
     */
    public void updateOrderStatus(int orderId, String newStatus) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId() == orderId) {
                orderList.get(i).setOrderStatus(newStatus);
                notifyItemChanged(i);
                break;
            }
        }
        // Also update in full list
        for (Order order : orderListFull) {
            if (order.getOrderId() == orderId) {
                order.setOrderStatus(newStatus);
                break;
            }
        }
    }

    /**
     * ViewHolder for order items.
     */
    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvShopName, tvDate, tvAmount, tvStatus;
        private Button btnViewDetails, btnCancelOrder, btnSubmitFeedback;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnCancelOrder = itemView.findViewById(R.id.btn_cancel_order);
            btnSubmitFeedback = itemView.findViewById(R.id.btn_submit_feedback);

            btnViewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onViewDetails(orderList.get(position));
                }
            });

            btnCancelOrder.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCancelOrder(orderList.get(position));
                }
            });

            btnSubmitFeedback.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSubmitFeedback(orderList.get(position));
                }
            });
        }

        void bind(Order order) {
            tvShopName.setText(order.getShopName());
            tvDate.setText(order.getFormattedDate());
            tvAmount.setText(order.getFormattedAmount());
            tvStatus.setText(order.getStatusDisplayText());
            tvStatus.setTextColor(order.getStatusColor());

            // Show/hide cancel button based on order status
            if (order.canBeCancelled()) {
                btnCancelOrder.setVisibility(View.VISIBLE);
            } else {
                btnCancelOrder.setVisibility(View.GONE);
            }

            // Show feedback button for delivered orders
            if (order.isDelivered()) {
                btnSubmitFeedback.setVisibility(View.VISIBLE);
                if (order.isFeedbackSubmitted()) {
                    btnSubmitFeedback.setText("✓ Feedback Submitted");
                    btnSubmitFeedback.setEnabled(false);
                    btnSubmitFeedback.setBackgroundColor(0xFF4A4A4A); // Gray background
                    btnSubmitFeedback.setTextColor(0xFFAAFFAA); // Light green text
                } else {
                    btnSubmitFeedback.setText("⭐ Submit Feedback");
                    btnSubmitFeedback.setEnabled(true);
                    btnSubmitFeedback.setBackgroundColor(0xFFFFB800); // Gold background
                    btnSubmitFeedback.setTextColor(0xFF000000); // Black text
                }
            } else {
                btnSubmitFeedback.setVisibility(View.GONE);
            }
        }
    }
}
