package com.quangtruong.appbanlinhkien.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.helper.CartHelper;
import com.quangtruong.appbanlinhkien.model.CartItem;
import com.quangtruong.appbanlinhkien.model.Product;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailProductActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription, productIngredients, quantityText;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView btnMinus, btnPlus;
    private int quantity = 1;
    private Button addToBasketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        productImage = findViewById(R.id.product_image_detail);
        productName = findViewById(R.id.product_name_detail);
        productPrice = findViewById(R.id.product_price_detail);
        productDescription = findViewById(R.id.product_description_detail);
        productIngredients = findViewById(R.id.product_ingredients);
        quantityText = findViewById(R.id.quantity_text);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        btnMinus = findViewById(R.id.minus_button);
        btnPlus = findViewById(R.id.plus_button);
        addToBasketButton = findViewById(R.id.add_to_basket_button);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Nhận dữ liệu từ Intent
        Product product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product != null) {
            // Set thông tin sản phẩm
            //collapsingToolbarLayout.setTitle(product.getProductName()); // xóa cái này
            productName.setText(product.getProductName());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPrice.setText(formatter.format(product.getUnitPrice()));
            productDescription.setText(product.getDescription());
            productIngredients.setText("One Pack Contains:\n"+product.getProductName()+" chất liệu cotton thoáng mát"); //Tạm thời để vậy
            quantityText.setText("1");

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(this)
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.placeholder_image);
            }

            btnMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    quantityText.setText(String.valueOf(quantity));
                }
            });

            btnPlus.setOnClickListener(v -> {
                quantity++;
                quantityText.setText(String.valueOf(quantity));
            });

            addToBasketButton.setOnClickListener(v -> {
                addToCart(product, quantity);
            });
        }
    }

    private void addToCart(Product product, int quantity) {
        CartItem cartItem = new CartItem(product, quantity);
        boolean added = CartHelper.addToCart(this, cartItem);
        if (added) {
            Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add to Cart", Toast.LENGTH_SHORT).show();
        }
    }
}