package com.quangtruong.appbanlinhkien.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.quangtruong.appbanlinhkien.MainActivity;
import com.quangtruong.appbanlinhkien.R;

public class OrderSuccessActivity extends AppCompatActivity {

    private Button btnTrackOrder, btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        btnTrackOrder = findViewById(R.id.btn_track_order);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);

        btnTrackOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Xử lý sự kiện khi nhấn nút "Track order"
                // Ví dụ: Chuyển đến màn hình theo dõi đơn hàng
            }
        });

        btnContinueShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện khi nhấn nút "Continue shopping"
                // Ví dụ: Quay về màn hình chính
                Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear activity stack
                startActivity(intent);
            }
        });
    }
}