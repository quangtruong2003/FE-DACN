package com.quangtruong.appbanlinhkien.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.quangtruong.appbanlinhkien.ActivityAdd;
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

public class ProductActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText searchEditText;
    private RecyclerView productRecyclerView;
    private FloatingActionButton fabAddProduct;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back
        toolbar.setNavigationOnClickListener(v -> finish());

        searchEditText = findViewById(R.id.search_edit_text);
        productRecyclerView = findViewById(R.id.product_list);
        fabAddProduct = findViewById(R.id.fab_add_product);

        apiService = new ApiUtils(this).createService(ApiService.class);

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productRecyclerView.setAdapter(productAdapter);

        loadProducts();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Lọc danh sách sản phẩm theo từ khóa tìm kiếm
                productAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến màn hình thêm sản phẩm
                Intent intent = new Intent(ProductActivity.this, ActivityAdd.class);
                startActivity(intent);
            }
        });
    }

    private void loadProducts() {
        // Gọi API lấy danh sách sản phẩm
        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful()) {
                    productList.clear();
                    productList.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                    Log.d("ProductActivity", "Loaded " + productList.size() + " products");
                } else {
                    Toast.makeText(ProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProductActivity", "Error loading products", t);
            }
        });
    }
}