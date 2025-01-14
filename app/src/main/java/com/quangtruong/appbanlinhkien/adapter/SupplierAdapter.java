package com.quangtruong.appbanlinhkien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.dto.SupplierDTO;

import java.util.ArrayList;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> implements Filterable {

    private List<SupplierDTO> supplierList;
    private List<SupplierDTO> supplierListFull; // Thêm danh sách đầy đủ cho việc filter
    private SupplierClickListener listener;

    public interface SupplierClickListener {
        void onSupplierClick(SupplierDTO supplier);
        void onSupplierLongClick(SupplierDTO supplier);
    }

    public SupplierAdapter(List<SupplierDTO> supplierList, SupplierClickListener listener) {
        this.supplierList = supplierList;
        this.supplierListFull = new ArrayList<>(supplierList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
        return new SupplierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
        SupplierDTO supplier = supplierList.get(position);
        holder.bind(supplier);
        holder.itemView.setOnClickListener(v -> listener.onSupplierClick(supplier));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onSupplierLongClick(supplier);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return supplierList.size();
    }

    static class SupplierViewHolder extends RecyclerView.ViewHolder {
        TextView supplierNameTextView, contactNameTextView, phoneTextView;

        public SupplierViewHolder(View itemView) {
            super(itemView);
            supplierNameTextView = itemView.findViewById(R.id.supplier_name);
            contactNameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.phone);
        }

        public void bind(SupplierDTO supplier) {
            supplierNameTextView.setText(supplier.getSupplierName());
            contactNameTextView.setText(supplier.getContactName());
            phoneTextView.setText(supplier.getPhone());
        }
    }

    public void setSuppliers(List<SupplierDTO> suppliers) {
        this.supplierList = suppliers;
        this.supplierListFull = new ArrayList<>(suppliers); // Cập nhật danh sách đầy đủ
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return supplierFilter;
    }

    private Filter supplierFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SupplierDTO> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(supplierListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (SupplierDTO item : supplierListFull) {
                    if (item.getSupplierName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            supplierList.clear();
            supplierList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}