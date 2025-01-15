package com.quangtruong.appbanlinhkien.admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.dto.CategoryDTO;
import com.quangtruong.appbanlinhkien.model.Category;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "com.quangtruong.appbanlinhkien.admin.EXTRA_CATEGORY";
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_EDIT = "EDIT";

    private TextInputEditText categoryNameEditText;
    private Button saveButton;
    private Button cancelButton;
    private String action;
    private Long categoryId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoryNameEditText = findViewById(R.id.category_name_edit_text);
        saveButton = findViewById(R.id.save_category_button);
        cancelButton = findViewById(R.id.cancel_button);

        apiService = ApiUtils.createService(ApiService.class);

        // Check if it's add or edit mode
        Intent intent = getIntent();
        action = intent.getAction();
        if (ACTION_EDIT.equals(action)) {
            categoryId = intent.getLongExtra(EXTRA_CATEGORY, -1);
            if (categoryId == -1) {
                Toast.makeText(this, "CategoryId is missing", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Load category details for editing
            loadCategoryDetails(categoryId);
        }

        //saveButton.setOnClickListener(v -> saveCategory());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategory();
            }
        });

        cancelButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void loadCategoryDetails(Long categoryId) {
        apiService.getCategoryById(categoryId).enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryDTO categoryDTO = response.body();
                    categoryNameEditText.setText(categoryDTO.getCategoryName());
                } else {
                    Toast.makeText(AddEditCategoryActivity.this, "Failed to load category details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
                Toast.makeText(AddEditCategoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to load category details", t);
            }
        });
    }

    private void saveCategory() {
        String categoryName = categoryNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(categoryName)) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        Category newCategory = new Category();
        newCategory.setCategoryName(categoryName);

        if (ACTION_ADD.equals(action)) {
            // Create a new Category object for adding
            apiService.createCategory(newCategory).enqueue(new Callback<CategoryDTO>() {
                @Override
                public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                    if (response.isSuccessful()) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        handleResponseError(response);
                    }
                }

                @Override
                public void onFailure(Call<CategoryDTO> call, Throwable t) {
                    handleFailure(t);
                }
            });
        } else {
            // Update existing category
            apiService.updateCategory(categoryId, newCategory).enqueue(new Callback<CategoryDTO>() {
                @Override
                public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                    if (response.isSuccessful()) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        handleResponseError(response);
                    }
                }

                @Override
                public void onFailure(Call<CategoryDTO> call, Throwable t) {
                    handleFailure(t);
                }
            });
        }
    }

    private void handleResponseError(Response<?> response) {
        Log.e("API_Error", "Response Code: " + response.code());
        try {
            Log.e("API_Error", "Response Body: " + response.errorBody().string());
        } catch (Exception e) {
            Log.e("API_Error", "Error reading response body", e);
        }
        Toast.makeText(AddEditCategoryActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
    }

    private void handleFailure(Throwable t) {
        Toast.makeText(AddEditCategoryActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("API_Error", "Failed to perform operation", t);
    }
}