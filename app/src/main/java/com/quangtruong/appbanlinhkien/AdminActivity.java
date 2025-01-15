package com.quangtruong.appbanlinhkien;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.quangtruong.appbanlinhkien.admin.CategoryActivity;
import com.quangtruong.appbanlinhkien.admin.CustomerActivity;
import com.quangtruong.appbanlinhkien.admin.OrderActivity;
import com.quangtruong.appbanlinhkien.admin.ProductActivity;
import com.quangtruong.appbanlinhkien.admin.SupplierActivity;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Kiểm tra đăng nhập
        if (!isLoggedIn()) {
            redirectToLogin();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MaterialCardView cardProducts = findViewById(R.id.card_products);
        MaterialCardView cardCategories = findViewById(R.id.card_categories);
        MaterialCardView cardOrders = findViewById(R.id.card_orders);
        MaterialCardView cardSuppliers = findViewById(R.id.card_suppliers);
        MaterialCardView cardCustomers = findViewById(R.id.card_customers);

        cardProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển đến màn hình quản lý sản phẩm
                Intent intent = new Intent(AdminActivity.this, ProductActivity.class);
                startActivity(intent);
            }
        });

        cardCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển đến màn hình quản lý danh mục
                Intent intent = new Intent(AdminActivity.this, CategoryActivity.class);
                startActivity(intent);
            }
        });

        cardOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển đến màn hình quản lý đơn hàng
                Intent intent = new Intent(AdminActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });
        cardSuppliers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển đến màn hình quản lý nhà cung cấp
                Intent intent = new Intent(AdminActivity.this, SupplierActivity.class);
                startActivity(intent);
            }
        });
        cardCustomers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, CustomerActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        // Xóa token khỏi SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("role"); // Xóa role
        editor.apply();

        // Chuyển về LoginActivity
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        String role = sharedPreferences.getString("role",null);
        // Check expired token
        if (token != null && role.equals("employee")) {
            return true;
        } else {
            return false;
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
    }
}