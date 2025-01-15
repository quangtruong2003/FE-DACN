package com.quangtruong.appbanlinhkien;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.quangtruong.appbanlinhkien.activity.CartActivity;
import com.quangtruong.appbanlinhkien.activity.DetailProductActivity;
import com.quangtruong.appbanlinhkien.adapter.ProductUserAdapter;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.helper.CartHelper;
import com.quangtruong.appbanlinhkien.model.CartItem;
import com.quangtruong.appbanlinhkien.model.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerViewRecommended, recyclerViewHottest;
    private ProductUserAdapter productAdapterRecommended, productAdapterHottest;
    private List<Product> productListRecommended = new ArrayList<>();
    private List<Product> productListHottest = new ArrayList<>();
    private ApiService apiService;
    private TabLayout tabLayout;
    private SearchView searchView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvMyCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiService = new ApiUtils(this).createService(ApiService.class);
        recyclerViewRecommended = findViewById(R.id.recyclerViewRecommended);
        recyclerViewHottest = findViewById(R.id.recyclerViewHottest);
        tabLayout = findViewById(R.id.tabLayout);
        searchView = findViewById(R.id.search_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        tvMyCart = findViewById(R.id.my_cart);
        tvMyCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productAdapterRecommended.getFilter().filter(query);
                productAdapterHottest.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapterRecommended.getFilter().filter(newText);
                productAdapterHottest.getFilter().filter(newText);
                return true;
            }
        });
        // Recommended
        recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productAdapterRecommended = new ProductUserAdapter(productListRecommended, this);
        recyclerViewRecommended.setAdapter(productAdapterRecommended);
        productAdapterRecommended.setOnProductClickListener(new ProductUserAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(MainActivity.this, DetailProductActivity.class);
                intent.putExtra("PRODUCT", product);
                startActivity(intent);
            }
            @Override
            public void onAddToCartClick(Product product, int quantity) {
                CartItem cartItem = new CartItem(product, quantity);
                if (CartHelper.addToCart(MainActivity.this, cartItem)) {
                    Toast.makeText(MainActivity.this, "Added to basket", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add to basket", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Hottest
        recyclerViewHottest.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productAdapterHottest = new ProductUserAdapter(productListHottest, this);
        recyclerViewHottest.setAdapter(productAdapterHottest);
        productAdapterHottest.setOnProductClickListener(new ProductUserAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(MainActivity.this, DetailProductActivity.class);
                intent.putExtra("PRODUCT", product);
                startActivity(intent);
            }
            @Override
            public void onAddToCartClick(Product product, int quantity) {
                CartItem cartItem = new CartItem(product, quantity);
                if (CartHelper.addToCart(MainActivity.this, cartItem)) {
                    Toast.makeText(MainActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Kiểm tra tablayout đã được khởi tạo
        if(tabLayout != null){
            tabLayout.addTab(tabLayout.newTab().setText("Hottest"));
            tabLayout.addTab(tabLayout.newTab().setText("Popular"));
            tabLayout.addTab(tabLayout.newTab().setText("New Combo"));
            tabLayout.addTab(tabLayout.newTab().setText("Top"));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // Xử lý filter
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        } else {
            Log.e("MainActivity", "tabLayout is null");
        }

        loadProducts();

        //Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        // Navigation Drawer
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        // Tính toán kích thước của NavigationView
        setNavigationViewWidth(0.8f); // Chiếm 80% chiều rộng màn hình

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
    }

    private void loadProducts() {
        Call<List<Product>> call = apiService.getAllProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productListRecommended.clear();
                    productListHottest.clear();

                    for (Product product : response.body()) {
                        // Lọc sản phẩm cho Recommended (ví dụ: categoryId = 1)
                        if (product.getCategoryId() != null && product.getCategoryId() == 1L) {
                            productListRecommended.add(product);
                        }
                        // Lọc sản phẩm cho Hottest (ví dụ: categoryId = 2)
                        if (product.getCategoryId() != null && product.getCategoryId() == 2L) {
                            productListHottest.add(product);
                        }
                    }

                    productAdapterRecommended.notifyDataSetChanged();
                    productAdapterHottest.notifyDataSetChanged();

                } else {
                    Toast.makeText(MainActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected( MenuItem item) {
        if(item.getItemId() == R.id.nav_logout){
            logout();

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("role");
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void setNavigationViewWidth(float percentage) {
        // Tính toán chiều rộng dựa trên phần trăm màn hình
        int width = (int) (getScreenWidth() * percentage);

        // Đặt layout params cho NavigationView
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = width;
        navigationView.setLayoutParams(params);
    }
    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Hiển thị tên và email trong Navigation Header
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.tv_username);
        TextView navEmail = headerView.findViewById(R.id.tv_email);

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "");
        String email = sharedPreferences.getString("email", "");

        Log.d("MainActivity", "onResume():");
        Log.d("MainActivity", "  role: " + role);
        Log.d("MainActivity", "  email: " + email);

        if (role.equals("customer")) {
            String name = sharedPreferences.getString("name", "");
            Log.d("MainActivity", "  name: " + name);
            navUsername.setText(name); // Gán name cho navUsername
            navEmail.setText(email); // Gán email cho navEmail
        } else if (role.equals("employee")) { // Thêm trường hợp cho employee
            String name = sharedPreferences.getString("fullname", ""); // Lấy fullname cho employee
            Log.d("MainActivity", "  name: " + name);
            navUsername.setText(name); // Gán name cho navUsername
            navEmail.setText(email); // Gán email cho navEmail
        } else {
            navUsername.setText("Guest");
            navEmail.setText("");
        }
    }
}