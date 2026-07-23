package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.CartItem;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.SIMATS.Groceryconnect.utils.CartManager;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * RecyclerView adapter for displaying cart items.
 */
public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartItemAdapter(List<CartItem> cartItems, OnCartChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvPrice, tvQuantity;
        private TextView btnDecrease, btnIncrease;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
        }

        void bind(CartItem item) {
            tvProductName.setText(item.getProduct().getProductName());
            tvPrice.setText(String.format("₹%.2f", item.getProduct().getPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Load product image from database
            String imageUrl = item.getProduct().getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String fullUrl = imageUrl.startsWith("http") ? imageUrl : ApiConfig.IMAGE_BASE_URL + imageUrl;
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Increase quantity
            btnIncrease.setOnClickListener(v -> {
                int newQty = item.getQuantity() + 1;
                item.setQuantity(newQty);
                CartManager.getInstance().updateQuantity(item.getProduct().getProductId(), newQty);
                tvQuantity.setText(String.valueOf(newQty));
                if (listener != null) listener.onCartChanged();
            });

            // Decrease quantity
            btnDecrease.setOnClickListener(v -> {
                int newQty = item.getQuantity() - 1;
                if (newQty <= 0) {
                    // Remove item
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        CartManager.getInstance().removeFromCart(item.getProduct().getProductId());
                        cartItems.remove(pos);
                        notifyItemRemoved(pos);
                        if (listener != null) listener.onCartChanged();
                    }
                } else {
                    item.setQuantity(newQty);
                    CartManager.getInstance().updateQuantity(item.getProduct().getProductId(), newQty);
                    tvQuantity.setText(String.valueOf(newQty));
                    if (listener != null) listener.onCartChanged();
                }
            });
        }
    }
}
