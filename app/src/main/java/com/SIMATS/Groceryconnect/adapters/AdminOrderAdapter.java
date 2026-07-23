package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.AdminOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying admin orders.
 */
public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private List<AdminOrder> orderList;
    private OnOrderActionListener listener;
    private boolean showingCompleted = false;

    public interface OnOrderActionListener {
        void onAcceptOrder(AdminOrder order);
        void onDeclineOrder(AdminOrder order);
        void onUpdateStatus(AdminOrder order, String newStatus);
        void onViewDetails(AdminOrder order);
    }

    public AdminOrderAdapter(List<AdminOrder> orderList, OnOrderActionListener listener) {
        this.orderList = new ArrayList<>(orderList);
        this.listener = listener;
    }

    public void setShowingCompleted(boolean showingCompleted) {
        this.showingCompleted = showingCompleted;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        AdminOrder order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    /**
     * Update the order list with new data.
     */
    public void updateOrders(List<AdminOrder> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged();
    }

    /**
     * Update a specific order's status.
     */
    public void updateOrderStatus(int orderId, String newStatus) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId() == orderId) {
                orderList.get(i).setOrderStatus(newStatus);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Remove an order from the list.
     */
    public void removeOrder(int orderId) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId() == orderId) {
                orderList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * ViewHolder for order items.
     */
    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvCustomerName, tvAmount, tvStatus;
        private TextView tvOrderType, tvOrderItems;
        private LinearLayout layoutPendingActions;
        private Button btnDecline, btnAccept, btnUpdateStatus, btnViewDetails;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvOrderType = itemView.findViewById(R.id.tv_order_type);
            tvOrderItems = itemView.findViewById(R.id.tv_order_items);
            layoutPendingActions = itemView.findViewById(R.id.layout_pending_actions);
            btnDecline = itemView.findViewById(R.id.btn_decline);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnUpdateStatus = itemView.findViewById(R.id.btn_update_status);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }

        void bind(AdminOrder order) {
            tvOrderId.setText(order.getFormattedOrderId());
            tvCustomerName.setText(order.getUserName());
            tvAmount.setText(order.getFormattedAmount());
            tvStatus.setText(order.getStatusDisplayText());

            // Show order type for delivery orders
            if (order.isDeliveryOrder()) {
                tvOrderType.setVisibility(View.VISIBLE);
                tvOrderType.setText("🚚 Delivery");
            } else {
                tvOrderType.setVisibility(View.VISIBLE);
                tvOrderType.setText("🏪 Pickup");
            }

            // Show order items
            tvOrderItems.setText(order.getItemsDisplayText());

            // Handle different order states
            if (showingCompleted) {
                // Completed tab - show only view details
                layoutPendingActions.setVisibility(View.GONE);
                btnUpdateStatus.setVisibility(View.GONE);
                btnViewDetails.setVisibility(View.VISIBLE);
                
                // Update status color for completed/cancelled
                if (AdminOrder.STATUS_CANCELLED.equalsIgnoreCase(order.getOrderStatus())) {
                    tvStatus.setTextColor(0xFFFF4444); // Red
                } else {
                    tvStatus.setTextColor(0xFF39FF14); // Green
                }
            } else if (order.isPending()) {
                // Pending orders - show accept/decline
                layoutPendingActions.setVisibility(View.VISIBLE);
                btnUpdateStatus.setVisibility(View.GONE);
                btnViewDetails.setVisibility(View.GONE);
                tvStatus.setTextColor(0xFFFF9800); // Orange
            } else if (order.isInProgress()) {
                // In progress - show status update button
                layoutPendingActions.setVisibility(View.GONE);
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnViewDetails.setVisibility(View.GONE);
                
                String nextStatusText = order.getNextStatusButtonText();
                if (nextStatusText != null) {
                    btnUpdateStatus.setText(nextStatusText);
                }
                tvStatus.setTextColor(0xFFFF9800); // Orange
            } else {
                // Default - hide all action buttons
                layoutPendingActions.setVisibility(View.GONE);
                btnUpdateStatus.setVisibility(View.GONE);
                btnViewDetails.setVisibility(View.VISIBLE);
            }


            // Click listeners
            btnDecline.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeclineOrder(order);
                }
            });

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptOrder(order);
                }
            });

            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    String nextStatus = order.getNextStatus();
                    if (nextStatus != null) {
                        listener.onUpdateStatus(order, nextStatus);
                    }
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(order);
                }
            });
        }
    }
}

