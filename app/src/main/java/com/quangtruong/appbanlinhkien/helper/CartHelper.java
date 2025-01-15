package com.quangtruong.appbanlinhkien.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quangtruong.appbanlinhkien.model.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartHelper {

    private static final String CART_PREF = "cart_pref";
    private static final String CART_ITEMS = "cart_items";

    public static boolean addToCart(Context context, CartItem cartItem) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(CART_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();

            // Lấy danh sách sản phẩm hiện tại trong giỏ hàng
            List<CartItem> cartItems = getCartItems(context);

            // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng chưa
            boolean found = false;
            for (int i = 0; i < cartItems.size(); i++) {
                if (cartItems.get(i).getProduct().getProductId().equals(cartItem.getProduct().getProductId())) {
                    // Nếu sản phẩm đã tồn tại, tăng số lượng
                    cartItems.get(i).setQuantity(cartItems.get(i).getQuantity() + cartItem.getQuantity());
                    found = true;
                    break;
                }
            }

            // Nếu sản phẩm chưa tồn tại, thêm mới vào giỏ hàng
            if (!found) {
                cartItems.add(cartItem);
            }

            // Lưu danh sách sản phẩm mới vào SharedPreferences
            String json = gson.toJson(cartItems);
            editor.putString(CART_ITEMS, json);
            editor.apply();

            return true;
        } catch (Exception e) {
            Log.e("CartHelper", "Error adding to cart: " + e.getMessage());
            return false;
        }
    }

    public static List<CartItem> getCartItems(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CART_PREF, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(CART_ITEMS, null);
        Gson gson = new Gson();

        if (json != null) {
            Type type = new TypeToken<List<CartItem>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }
}