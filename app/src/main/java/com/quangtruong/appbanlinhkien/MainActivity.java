package com.quangtruong.appbanlinhkien;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.quangtruong.appbanlinhkien.R;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView menuIcon, cartIcon, profileIcon,addItem1, item1FavoriteIcon;
    TextView cartBadge;
    TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        menuIcon = findViewById(R.id.menu_icon);
        cartIcon = findViewById(R.id.cart_icon);
        profileIcon = findViewById(R.id.profile_icon);
        searchEditText = findViewById(R.id.search_edit_text);
        addItem1 = findViewById(R.id.item1_add_icon);
        cartBadge = findViewById(R.id.cart_badge);
        item1FavoriteIcon = findViewById(R.id.item1_favorite_icon);

        // Xử lý sự kiện click cho các icon (tạm thời hiển thị Toast)
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Menu Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Cart Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        addItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Add item 1 Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Ẩn/hiện badge
        cartBadge.setVisibility(View.VISIBLE); // Hoặc GONE để ẩn
    }
}