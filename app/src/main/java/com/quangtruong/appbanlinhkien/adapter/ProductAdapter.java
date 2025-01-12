package com.quangtruong.appbanlinhkien.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private List<Product> productList;
    private List<Product> productListFiltered;
    private Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.productListFiltered = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productListFiltered.get(position);
        holder.productNameTextView.setText(product.getProductName());
        holder.productCategoryTextView.setText(String.format("Danh mục: %s", product.getCategoryName()));
        holder.productSupplierTextView.setText(String.format("Nhà cung cấp: %s", product.getSupplierName()));
        // Định dạng số tiền
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(product.getUnitPrice());
        holder.productPriceTextView.setText(String.format("Giá: %s", formattedPrice));
        // Load hình ảnh sử dụng Glide
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.placeholder_image) // Ảnh placeholder
                    .error(R.drawable.error_image) // Ảnh lỗi
                    .into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.placeholder_image); // Ảnh mặc định
        }
    }

    @Override
    public int getItemCount() {
        return productListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    productListFiltered = productList;
                } else {
                    List<Product> filteredList = new ArrayList<>();
                    for (Product product : productList) {
                        if (product.getProductName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(product);
                        }
                    }
                    productListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = productListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                productListFiltered = (ArrayList<Product>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView productCategoryTextView;
        TextView productSupplierTextView;
        TextView productPriceTextView;
        ImageView productImageView;
        public ProductViewHolder(View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.product_name);
            productCategoryTextView = itemView.findViewById(R.id.product_category);
            productSupplierTextView = itemView.findViewById(R.id.product_supplier);
            productPriceTextView = itemView.findViewById(R.id.product_price);
            productImageView = itemView.findViewById(R.id.product_image);
            // Khởi tạo các view khác nếu cần
        }
    }
}