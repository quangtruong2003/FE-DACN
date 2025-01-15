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
import com.quangtruong.appbanlinhkien.dto.SupplierDTO;
import com.quangtruong.appbanlinhkien.model.Category;
import com.quangtruong.appbanlinhkien.model.Supplier;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditSupplierActivity extends AppCompatActivity {

    public static final String EXTRA_SUPPLIER_ID = "com.quangtruong.appbanlinhkien.admin.EXTRA_SUPPLIER_ID";
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_EDIT = "EDIT";

    private TextInputEditText supplierNameEditText, contactNameEditText, addressEditText, phoneEditText, emailEditText, websiteEditText;
    private Button saveButton;
    private Button cancelButton;
    private String action;
    private Long supplierId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_supplier);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        supplierNameEditText = findViewById(R.id.supplier_name_edit_text);
        contactNameEditText = findViewById(R.id.contact_name_edit_text);
        addressEditText = findViewById(R.id.address_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        websiteEditText = findViewById(R.id.website_edit_text);
        saveButton = findViewById(R.id.save_supplier_button);
        cancelButton = findViewById(R.id.cancel_button);

        apiService = ApiUtils.createService(ApiService.class);

        // Check if it's add or edit mode
        Intent intent = getIntent();
        action = intent.getAction();
        if (ACTION_EDIT.equals(action)) {
            supplierId = intent.getLongExtra(EXTRA_SUPPLIER_ID, -1);
            if (supplierId != -1) {
                loadSupplierDetails(supplierId);
            } else {
                Toast.makeText(this, "Supplier ID is missing", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        saveButton.setOnClickListener(v -> saveSupplier());

        cancelButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void loadSupplierDetails(Long supplierId) {
        apiService.getSupplierById(supplierId).enqueue(new Callback<SupplierDTO>() {
            @Override
            public void onResponse(Call<SupplierDTO> call, Response<SupplierDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupplierDTO supplierDTO = response.body();
                    supplierNameEditText.setText(supplierDTO.getSupplierName());
                    contactNameEditText.setText(supplierDTO.getContactName());
                    addressEditText.setText(supplierDTO.getAddress());
                    phoneEditText.setText(supplierDTO.getPhone());
                    emailEditText.setText(supplierDTO.getEmail());
                    websiteEditText.setText(supplierDTO.getWebsite());
                } else {
                    Toast.makeText(AddEditSupplierActivity.this, "Failed to load supplier details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SupplierDTO> call, Throwable t) {
                Toast.makeText(AddEditSupplierActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to load supplier details", t);
            }
        });
    }

    private void saveSupplier() {
        String supplierName = supplierNameEditText.getText().toString().trim();
        String contactName = contactNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String website = websiteEditText.getText().toString().trim();

        if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, "Vui lòng nhập tên nhà cung cấp", Toast.LENGTH_SHORT).show();
            return;
        }

        Supplier newSupplier = new Supplier();
        newSupplier.setSupplierName(supplierName);
        newSupplier.setContactName(contactName);
        newSupplier.setAddress(address);
        newSupplier.setPhone(phone);
        newSupplier.setEmail(email);
        newSupplier.setWebsite(website);

        if (ACTION_ADD.equals(action)) {
            apiService.createSupplier(newSupplier).enqueue(new Callback<SupplierDTO>() {
                @Override
                public void onResponse(Call<SupplierDTO> call, Response<SupplierDTO> response) {
                    if (response.isSuccessful()) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        handleResponseError(response);
                    }
                }

                @Override
                public void onFailure(Call<SupplierDTO> call, Throwable t) {
                    handleFailure(t);
                }
            });
        } else {
            apiService.updateSupplier(supplierId, newSupplier).enqueue(new Callback<SupplierDTO>() {
                @Override
                public void onResponse(Call<SupplierDTO> call, Response<SupplierDTO> response) {
                    if (response.isSuccessful()) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        handleResponseError(response);
                    }
                }

                @Override
                public void onFailure(Call<SupplierDTO> call, Throwable t) {
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
        Toast.makeText(AddEditSupplierActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
    }

    private void handleFailure(Throwable t) {
        Toast.makeText(AddEditSupplierActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("API_Error", "Failed to perform operation", t);
    }
}