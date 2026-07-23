package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Shop;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying shop items.
 */
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Shop> shopList;
    private List<Shop> shopListFull; // For filtering
    private OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    public ShopAdapter(List<Shop> shopList, OnShopClickListener listener) {
        // Create our own internal copies to avoid reference issues
        this.shopList = new ArrayList<>(shopList);
        this.shopListFull = new ArrayList<>(shopList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.bind(shop);
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    /**
     * Update the shop list with new data.
     */
    public void updateShops(List<Shop> newShops) {
        this.shopList.clear();
        this.shopList.addAll(newShops);
        this.shopListFull.clear();
        this.shopListFull.addAll(newShops);
        notifyDataSetChanged();
    }

    /**
     * Filter shops by search query.
     */
    public void filter(String query) {
        shopList.clear();
        if (query.isEmpty()) {
            shopList.addAll(shopListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Shop shop : shopListFull) {
                if (shop.getShopName().toLowerCase().contains(lowerQuery) ||
                    shop.getCategory().toLowerCase().contains(lowerQuery)) {
                    shopList.add(shop);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for shop items.
     */
    class ShopViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivShopImage;
        private TextView tvShopName, tvStatusBadge, tvDistance, tvDelivery, tvRating;

        ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivShopImage = itemView.findViewById(R.id.iv_shop_image);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvDelivery = itemView.findViewById(R.id.tv_delivery);
            tvRating = itemView.findViewById(R.id.tv_rating);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShopClick(shopList.get(position));
                }
            });
        }

        void bind(Shop shop) {
            tvShopName.setText(shop.getShopName());
            
            // Load shop image
            String shopImageUrl = shop.getShopImage();
            if (shopImageUrl != null && !shopImageUrl.isEmpty()) {
                String fullUrl = shopImageUrl.startsWith("http") ? shopImageUrl : ApiConfig.IMAGE_BASE_URL + shopImageUrl;
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_shop_placeholder)
                        .error(R.drawable.ic_shop_placeholder)
                        .circleCrop()
                        .into(ivShopImage);
            } else {
                ivShopImage.setImageResource(R.drawable.ic_shop_placeholder);
            }
            
            // Set open/closed status
            if (shop.isOpen()) {
                tvStatusBadge.setText("Open");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_open);
                tvStatusBadge.setTextColor(0xFF000000);
            } else {
                tvStatusBadge.setText("Closed");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_closed);
                tvStatusBadge.setTextColor(0xFFFFFFFF);
            }

            // Distance (placeholder - could calc from lat/lng)
            tvDistance.setText("📍 Nearby");

            // Delivery status
            if (shop.isDeliveryAvailable()) {
                tvDelivery.setText("🚚 Delivery Available");
            } else {
                tvDelivery.setText("🛍️ Pickup Only");
            }

            // Rating - show actual rating or message if no ratings
            if (shop.getRatingCount() > 0) {
                tvRating.setText(String.format("⭐ %.1f (%d)", shop.getRating(), shop.getRatingCount()));
            } else {
                tvRating.setText("No reviews yet");
            }
        }
    }
}
