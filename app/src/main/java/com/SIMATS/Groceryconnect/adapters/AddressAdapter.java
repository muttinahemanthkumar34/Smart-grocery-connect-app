package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Address;

import java.util.List;

/**
 * Adapter for displaying user addresses in a RecyclerView.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditClick(Address address);
        void onDeleteClick(Address address);
    }

    public AddressAdapter(List<Address> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    public void updateList(List<Address> newList) {
        this.addressList = newList;
        notifyDataSetChanged();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLabel, tvAddressLine, tvCityPincode;
        private ImageView ivEdit, ivDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tv_label);
            tvAddressLine = itemView.findViewById(R.id.tv_address_line);
            tvCityPincode = itemView.findViewById(R.id.tv_city_pincode);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(Address address) {
            tvLabel.setText(address.getLabel());
            tvAddressLine.setText(address.getAddressLine());
            tvCityPincode.setText(address.getCity() + ", " + address.getPincode());

            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(address);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(address);
                }
            });
        }
    }
}
