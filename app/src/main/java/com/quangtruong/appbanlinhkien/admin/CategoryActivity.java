package com.quangtruong.appbanlinhkien.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.adapter.CategoryAdapter;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.dto.CategoryDTO;
import com.quangtruong.appbanlinhkien.model.Category;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.CategoryClickListener {

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<CategoryDTO> categoryList;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddCategory;
    private ActivityResultLauncher<Intent> addCategoryLauncher;
    private ActivityResultLauncher<Intent> editCategoryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        categoryRecyclerView = findViewById(R.id.category_list);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        fabAddCategory = findViewById(R.id.fab_add_category);

        apiService = new ApiUtils(this).createService(ApiService.class);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setAdapter(categoryAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadCategories);

        addCategoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadCategories();
                    }
                });

        editCategoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadCategories();
                    }
                }
        );

        fabAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, AddEditCategoryActivity.class);
            intent.setAction(AddEditCategoryActivity.ACTION_ADD);
            addCategoryLauncher.launch(intent);
        });

        loadCategories();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (categoryAdapter != null) {
                    // Chỉ filter khi newText không rỗng
                    if (!newText.isEmpty()) {
                        categoryAdapter.getFilter().filter(newText);
                    } else {
                        // Nếu newText rỗng, hiển thị lại toàn bộ danh sách
                        categoryAdapter.setCategories(categoryList);
                    }
                }
                return true;
            }
        });

        return true;
    }

    private void loadCategories() {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getAllCategories().enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    categoryList.clear();
                    List<CategoryDTO> categories = response.body();
                    if (categories != null && !categories.isEmpty()) {
                        categoryList.addAll(categories);
                        categoryAdapter.notifyDataSetChanged();
                        Log.d("CategoryActivity", "Loaded " + categoryList.size() + " categories");
                    }
                } else {
                    Toast.makeText(CategoryActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(CategoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CategoryActivity", "Error loading categories", t);
            }
        });
    }

    @Override
    public void onCategoryClick(CategoryDTO category) {
        Intent intent = new Intent(this, AddEditCategoryActivity.class);
        intent.setAction(AddEditCategoryActivity.ACTION_EDIT);
        intent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY, category.getCategoryId()); //Truyền categoryId
        editCategoryLauncher.launch(intent);
    }

    @Override
    public void onCategoryLongClick(CategoryDTO category) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục " + category.getCategoryName() + "?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteCategory(category.getCategoryId())) // Sửa thành category.getCategoryId()
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteCategory(Long categoryId) {
        apiService.deleteCategory(categoryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoryActivity.this, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Refresh the list after deletion
                } else {
                    Toast.makeText(CategoryActivity.this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CategoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}