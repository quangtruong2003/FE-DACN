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
import com.quangtruong.appbanlinhkien.dto.CategoryDTO;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> implements Filterable {

    private List<CategoryDTO> categoryList;
    private List<CategoryDTO> categoryListFull; // Danh sách đầy đủ, không bị filter
    private CategoryClickListener listener;

    public interface CategoryClickListener {
        void onCategoryClick(CategoryDTO category);
        void onCategoryLongClick(CategoryDTO category);
    }

    public CategoryAdapter(List<CategoryDTO> categoryList, CategoryClickListener listener) {
        this.categoryList = categoryList;
        this.categoryListFull = new ArrayList<>(categoryList); // Tạo bản sao của categoryList
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryDTO category = categoryList.get(position);
        holder.bind(category);
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onCategoryLongClick(category);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, productCountTextView;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            productCountTextView = itemView.findViewById(R.id.product_count);
        }

        public void bind(CategoryDTO category) {
            categoryName.setText(category.getCategoryName());
            productCountTextView.setText("Số sản phẩm: " + category.getProductCount());
        }
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categoryList = new ArrayList<>(categories);
        this.categoryListFull = new ArrayList<>(categories);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return categoryFilter;
    }

    private Filter categoryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CategoryDTO> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(categoryListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (CategoryDTO item : categoryListFull) {
                    if (item.getCategoryName().toLowerCase().contains(filterPattern)) {
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
            categoryList.clear();
            categoryList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}