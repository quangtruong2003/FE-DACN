package com.quangtruong.appbanlinhkien;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.model.AuthResponse;
import com.quangtruong.appbanlinhkien.model.LoginRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Kiểm tra trạng thái đăng nhập
        checkLoginStatus();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        apiService = new ApiUtils(this).createService(ApiService.class);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                login(email, password);
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        Log.d("LOGIN", "Email: " + email + ", Password: " + password);

        Call<AuthResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    AuthResponse authResponse = response.body();
                    String token = authResponse.getJwt();
                    String message = authResponse.getMessage();
                    Log.d("LOGIN", "Email: " + email);

                    Log.d("LOGIN", "Response: " + response.body().toString());
                    Log.d("LOGIN", "Response code: " + response.code());
                    Log.d("LOGIN", "Token: " + token);
                    Log.d("LOGIN", "Message: " + message);

                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("token", token);
                    myEdit.putString("role", message);
                    myEdit.putString("email", email);

                    if(message.equals("customer")){
                        myEdit.putString("name", email);
                    } else {
                        myEdit.putString("name", "Admin");
                    }
                    myEdit.apply();

                    // Log thông tin lưu vào SharedPreferences
                    Log.d("LoginActivity", "Saved to SharedPreferences:");
                    Log.d("LoginActivity", "  token: " + token);
                    Log.d("LoginActivity", "  role: " + message);
                    Log.d("LoginActivity", "  email: " + email);
                    Log.d("LoginActivity", "  name: " + sharedPreferences.getString("name", ""));

                    // Chuyển activity dựa vào role
                    if ("employee".equals(message)) {
                        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                        startActivity(intent);
                        // finish();
                    } else if ("customer".equals(message)) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        // finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi không xác định", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // Xử lý lỗi đăng nhập
                    try {
                        Log.d("LOGIN", "Response error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("LOGIN", "Response code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Log lỗi
                Log.e("LOGIN", "Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        String role = sharedPreferences.getString("role", null);

        if (token != null && role != null) {
            // Đã đăng nhập
            if (role.equals("employee")) {
                // Chuyển đến AdminActivity
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            } else if (role.equals("customer")) {
                // Chuyển đến MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}