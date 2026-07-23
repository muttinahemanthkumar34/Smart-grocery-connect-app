package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    public interface OnProductActionListener {
        void onEditProduct(AdminProductItem product);
        void onRestockProduct(AdminProductItem product);
        void onDeleteProduct(AdminProductItem product);
    }

    private List<AdminProductItem> products;
    private List<AdminProductItem> allProducts;
    private final OnProductActionListener listener;
    private String baseImageUrl = "";

    public AdminProductAdapter(List<AdminProductItem> products, OnProductActionListener listener) {
        this.products = new ArrayList<>(products);
        this.allProducts = new ArrayList<>(products);
        this.listener = listener;
    }

    public void setBaseImageUrl(String url) {
        this.baseImageUrl = url;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        AdminProductItem product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<AdminProductItem> newProducts) {
        this.allProducts = new ArrayList<>(newProducts);
        this.products = new ArrayList<>(newProducts);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        products.clear();
        if (query.isEmpty()) {
            products.addAll(allProducts);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AdminProductItem product : allProducts) {
                if (product.getProductName().toLowerCase().contains(lowerQuery)) {
                    products.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        products.clear();
        if (category.equalsIgnoreCase("All")) {
            products.addAll(allProducts);
        } else {
            for (AdminProductItem product : allProducts) {
                if (product.getCategory().equalsIgnoreCase(category)) {
                    products.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, btnEdit, btnRestock, btnDelete;
        TextView tvProductName, tvStockQuantity, tvLowStockWarning, tvPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvStockQuantity = itemView.findViewById(R.id.tv_stock_quantity);
            tvLowStockWarning = itemView.findViewById(R.id.tv_low_stock_warning);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnRestock = itemView.findViewById(R.id.btn_restock);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(AdminProductItem product) {
            tvProductName.setText(product.getProductName());
            tvStockQuantity.setText("Stock: " + product.getStockQuantity() + " units");
            tvPrice.setText(String.format("₹%.2f", product.getPrice()));

            // Low stock warning
            if (product.isLowStock()) {
                tvLowStockWarning.setVisibility(View.VISIBLE);
            } else {
                tvLowStockWarning.setVisibility(View.GONE);
            }

            // Load product image
            String imageUrl = product.getProductImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String fullUrl = imageUrl.startsWith("http") ? imageUrl : baseImageUrl + imageUrl;
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditProduct(product);
            });

            btnRestock.setOnClickListener(v -> {
                if (listener != null) listener.onRestockProduct(product);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteProduct(product);
            });
        }
    }

    // Inner class for product item data
    public static class AdminProductItem {
        private int productId;
        private String productName;
        private String category;
        private double price;
        private int stockQuantity;
        private String productImage;
        private boolean lowStock;

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }

        public boolean isLowStock() { return lowStock || stockQuantity < 10; }
        public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
    }
}
