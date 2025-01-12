package com.quangtruong.appbanlinhkien.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
    private ProductClickListener productClickListener;

    public interface ProductClickListener {
        void onProductClick(Long productId);
        void onProductLongClick(Long productId, String productName);
    }

    // Constructor với tham số ProductClickListener
    public ProductAdapter(List<Product> productList, Context context, ProductClickListener listener) {
        this.productList = productList;
        this.productListFiltered = productList;
        this.context = context;
        this.productClickListener = listener;
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
        // Debug: Log các giá trị để kiểm tra
        Log.d("ProductAdapter", "onBindViewHolder: position=" + position);
        if (product != null) {
            Log.d("ProductAdapter", "onBindViewHolder: product.getProductID()=" + product.getProductId());
            Log.d("ProductAdapter", "onBindViewHolder: product.getProductName()=" + product.getProductName());
        } else {
            Log.e("ProductAdapter", "onBindViewHolder: product is null at position " + position);
        }

        holder.productId = product.getProductId();
        holder.tvProductName.setText(product.getProductName());
        holder.tvProductCategory.setText(String.format("Danh mục: %s", product.getCategoryName()));
        holder.tvProductSupplier.setText(String.format("Nhà cung cấp: %s", product.getSupplierName()));
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(product.getUnitPrice());
        holder.tvProductPrice.setText(String.format("Giá: %s", formattedPrice));

        // Hiển thị trạng thái hoạt động
        if (product.active()) {
            holder.tvProductStatus.setText(R.string.active);
            holder.tvProductStatus.setTextColor(context.getResources().getColor(R.color.green)); // Màu xanh
            holder.tvProductStatus.setTypeface(null, Typeface.BOLD); // In đậm
        } else {
            holder.tvProductStatus.setText(R.string.inactive);
            holder.tvProductStatus.setTextColor(context.getResources().getColor(R.color.red)); // Màu đỏ
            holder.tvProductStatus.setTypeface(null, Typeface.BOLD); // In đậm
        }
        // Hiển thị hình ảnh
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set listener cho item view
        holder.itemView.setOnClickListener(holder);
        holder.itemView.setOnLongClickListener(holder);
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

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Long productId;
        TextView tvProductName;
        TextView tvProductCategory;
        TextView tvProductSupplier;
        TextView tvProductPrice;
        ImageView ivProductImage;
        TextView tvProductStatus;

        public ProductViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.product_name);
            tvProductCategory = itemView.findViewById(R.id.product_category);
            tvProductSupplier = itemView.findViewById(R.id.product_supplier);
            tvProductPrice = itemView.findViewById(R.id.product_price);
            ivProductImage = itemView.findViewById(R.id.product_image);
            tvProductStatus = itemView.findViewById(R.id.product_status); // Ánh xạ view
        }

        @Override
        public void onClick(View v) {
            if (productClickListener != null) {
                productClickListener.onProductClick(productId);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (productClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    productClickListener.onProductLongClick(productId, productListFiltered.get(position).getProductName());
                    return true;
                }
            }
            return false;
        }
    }
}