package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Product;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter for displaying saved items in a list
 */
public class SavedItemsAdapter extends RecyclerView.Adapter<SavedItemsAdapter.SavedItemViewHolder> {

    private List<Product> savedItems;
    private OnItemRemovedListener listener;

    public interface OnItemRemovedListener {
        void onItemRemoved(Product product, int position);
    }

    public SavedItemsAdapter(List<Product> savedItems, OnItemRemovedListener listener) {
        this.savedItems = savedItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_product, parent, false);
        return new SavedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedItemViewHolder holder, int position) {
        Product product = savedItems.get(position);
        holder.bind(product, position);
    }

    @Override
    public int getItemCount() {
        return savedItems.size();
    }

    class SavedItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvShopName;
        private TextView tvPrice;
        private ImageView ivRemove;

        SavedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            ivRemove = itemView.findViewById(R.id.iv_remove);
        }

        void bind(Product product, int position) {
            tvProductName.setText(product.getProductName());
            tvShopName.setText(product.getShopName());
            tvPrice.setText(String.format("₹%.2f", product.getPrice()));

            // Load product image
            String imageUrl = product.getImageUrl();
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

            // Remove button click
            ivRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(product, position);
                }
            });
        }
    }
}
