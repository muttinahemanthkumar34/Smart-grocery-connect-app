package com.SIMATS.Groceryconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SIMATS.Groceryconnect.R;
import com.SIMATS.Groceryconnect.models.Address;

import java.util.List;

/**
 * Adapter for selecting an address in cart.
 */
public class AddressSelectAdapter extends RecyclerView.Adapter<AddressSelectAdapter.ViewHolder> {

    private List<Address> addressList;
    private int selectedPosition = 0;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public AddressSelectAdapter(List<Address> addressList, OnAddressSelectedListener listener) {
        this.addressList = addressList;
        this.listener = listener;
        
        // Find default address position
        for (int i = 0; i < addressList.size(); i++) {
            if (addressList.get(i).isDefault()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address, position);
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    public Address getSelectedAddress() {
        if (addressList != null && selectedPosition >= 0 && selectedPosition < addressList.size()) {
            return addressList.get(selectedPosition);
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RadioButton rbSelect;
        private TextView tvLabel, tvAddressLine, tvCityPincode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rbSelect = itemView.findViewById(R.id.rb_select);
            tvLabel = itemView.findViewById(R.id.tv_label);
            tvAddressLine = itemView.findViewById(R.id.tv_address_line);
            tvCityPincode = itemView.findViewById(R.id.tv_city_pincode);
        }

        public void bind(Address address, int position) {
            tvLabel.setText(address.getLabel());
            tvAddressLine.setText(address.getAddressLine());
            tvCityPincode.setText(address.getCity() + ", " + address.getPincode());
            rbSelect.setChecked(position == selectedPosition);

            View.OnClickListener clickListener = v -> {
                int previousSelected = selectedPosition;
                selectedPosition = getAdapterPosition();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    listener.onAddressSelected(address);
                }
            };

            rbSelect.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }
    }
}
