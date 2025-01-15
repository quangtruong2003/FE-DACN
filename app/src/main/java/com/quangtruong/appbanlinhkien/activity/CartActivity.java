package com.quangtruong.appbanlinhkien.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.adapter.CartItemAdapter;
import com.quangtruong.appbanlinhkien.helper.CartHelper;
import com.quangtruong.appbanlinhkien.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;
    private RecyclerView cartRecyclerView;
    private CartItemAdapter cartItemAdapter;
    private List<CartItem> cartItemList;
    private TextView totalPrice;
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        cartRecyclerView = findViewById(R.id.cart_items_recycler_view);
        totalPrice = findViewById(R.id.total_price);
        TextView tvGoBack = findViewById(R.id.tv_go_back);
        tvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        cartItemList = CartHelper.getCartItems(this);

        cartItemAdapter = new CartItemAdapter(cartItemList, this); // Tạo adapter
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartItemAdapter);

        calculateTotalPrice();
        findViewById(R.id.checkout_button).setOnClickListener(v -> showCheckoutDialog());
    }

    private void calculateTotalPrice() {
        double total = 0;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (CartItem item : cartItemList) {
            if(item.getProduct() != null){
                total += item.getProduct().getUnitPrice().doubleValue() * item.getQuantity();
            }
        }
        totalPrice.setText(formatter.format(total));
    }

    // TODO: Thêm các phương thức để xử lý thêm, sửa, xóa sản phẩm trong giỏ hàng
    // Ví dụ:
    // public void addCartItem(CartItem item) { ... }
    // public void removeCartItem(CartItem item) { ... }
    // public void updateCartItemQuantity(CartItem item, int quantity) { ... }

    private void showCheckoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_checkout, null);

        // Ánh xạ các view trong dialog
        EditText edtAddress = view.findViewById(R.id.edt_address);
        EditText edtPhone = view.findViewById(R.id.edt_phone);
        Button btnPayOnDelivery = view.findViewById(R.id.btn_pay_on_delivery);
        Button btnPayWithCard = view.findViewById(R.id.btn_pay_with_card);

        // Set sự kiện cho các nút (nếu cần)
        btnPayOnDelivery.setOnClickListener(v -> {
            // Xử lý thanh toán khi nhận hàng

            dialog.dismiss();
            Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
            startActivity(intent);
        });

        btnPayWithCard.setOnClickListener(v -> {
            // Xử lý thanh toán bằng thẻ
            dialog.dismiss();
            Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
            startActivity(intent);
        });

        builder.setView(view);
        dialog = builder.create();

        //Bo tròn dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }
    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }
}