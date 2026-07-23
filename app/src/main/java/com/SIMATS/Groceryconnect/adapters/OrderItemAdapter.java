package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.OrderItem;
import com.SIMATS.Groceryconnect.network.ApiConfig;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {

    private List<OrderItem> itemList;

    public OrderItemAdapter(List<OrderItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        OrderItem item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvQuantity, tvPrice;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }

        void bind(OrderItem item) {
            tvProductName.setText(item.getProductName());
            tvQuantity.setText("Qty: " + item.getQuantity());
            tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getSubtotal()));

            // Load product image
            String imageUrl = item.getProductImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String fullUrl = imageUrl.startsWith("http") ? imageUrl : ApiConfig.IMAGE_BASE_URL + imageUrl;
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_shop_placeholder)
                        .error(R.drawable.ic_shop_placeholder)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_shop_placeholder);
            }
        }
    }
}
