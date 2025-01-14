package com.quangtruong.appbanlinhkien.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;
import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.adapter.ProductAdapter;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.model.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends AppCompatActivity implements ProductAdapter.ProductClickListener {

    private Toolbar toolbar;
    private RecyclerView productRecyclerView;
    private FloatingActionButton fabAddProduct;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> addProductLauncher;
    private ActivityResultLauncher<Intent> editProductLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        productRecyclerView = findViewById(R.id.product_list);
        fabAddProduct = findViewById(R.id.fab_add_product);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        apiService = new ApiUtils(this).createService(ApiService.class);

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this, this);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productRecyclerView.setAdapter(productAdapter);

        addProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        refreshData();
                    }
                });

        editProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        refreshData();
                    }
                }
        );

        loadProducts();

        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductActivity.this, ProductController.class);
            addProductLauncher.launch(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
        });

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
                if (productAdapter != null) {
                    // Chỉ filter khi newText không rỗng
                    if (!newText.isEmpty()) {
                        productAdapter.getFilter().filter(newText);
                    } else {
                        // Nếu newText rỗng, hiển thị lại toàn bộ danh sách
                        productAdapter.setProducts(productList);
                    }
                }
                return true;
            }
        });

        return true;
    }

    private void refreshData() {
        productList.clear();
        productAdapter.notifyDataSetChanged();
        loadProducts();
    }

    private void loadProducts() {
        swipeRefreshLayout.setRefreshing(true);

        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()) {
                    productList.clear();
                    List<Product> newProducts = response.body();
                    if (newProducts != null && !newProducts.isEmpty()) {
                        productList.addAll(newProducts);
                        productAdapter.notifyDataSetChanged();
                        Log.d("ProductActivity", "Loaded " + productList.size() + " products");
                    }
                } else {
                    Toast.makeText(ProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProductActivity", "Error loading products", t);
            }
        });
    }

    @Override
    public void onProductClick(Long productId) {
        Log.d("ProductActivity", "onProductClick: productId=" + productId); // Log productId
        Intent intent = new Intent(this, ProductController.class);
        intent.putExtra("action", "edit");
        intent.putExtra("PRODUCT_ID", productId);
        editProductLauncher.launch(intent);
    }

    @Override
    public void onProductLongClick(Long productId, String productName) {
        Log.d("ProductActivity", "onProductLongClick: productId=" + productId + ", productName=" + productName);
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm " + productName + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Tiếp tục với xóa
                        deleteProduct(productId);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                //.setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct(Long productId) {
        if (productId == null) {
            Log.e("ProductActivity", "deleteProduct: productId is null");
            return;
        }
        apiService.deleteProduct(productId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("deleteProduct", response.toString());
                if (response.isSuccessful()) {
                    Toast.makeText(ProductActivity.this, "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    Toast.makeText(ProductActivity.this, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Delete product failed", t);
            }
        });
    }
}