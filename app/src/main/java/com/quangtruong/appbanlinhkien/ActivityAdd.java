package com.quangtruong.appbanlinhkien;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.dto.ProductDTO;
import com.quangtruong.appbanlinhkien.model.Category;
import com.quangtruong.appbanlinhkien.model.Product;
import com.quangtruong.appbanlinhkien.model.Supplier;
import com.quangtruong.appbanlinhkien.request.CreateProductRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAdd extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private TextInputEditText productNameEditText, productCategoryEditText, productSupplierEditText,
            productPriceEditText, productStockEditText, productDescriptionEditText;
    private Spinner productCategorySpinner, productSupplierSpinner;
    private ImageView productImageView;
    private SwitchMaterial productActiveSwitch;
    private Button addProductButton, chooseImageButton, takePhotoButton;
    private ApiService apiService;
    private List<Category> categoryList;
    private List<Supplier> supplierList;
    private String selectedImageBase64;
    private Bitmap selectedImageBitmap;

    private Long selectedCategoryId;
    private Long selectedSupplierId;
    private ActivityResultLauncher<Intent> chooseImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri imageUri = data.getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
                            productImageView.setImageBitmap(selectedImageBitmap);
                            productImageView.setVisibility(View.VISIBLE);
                            // Không cần convert sang base64 nữa
                            // selectedImageBase64 = convertBitmapToBase64(selectedImageBitmap);
                            // Log.d("Base64 Image", selectedImageBase64);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    productImageView.setImageBitmap(selectedImageBitmap);
                    productImageView.setVisibility(View.VISIBLE);
                    // Không cần convert sang base64 nữa
                    // selectedImageBase64 = convertBitmapToBase64(selectedImageBitmap);
                    // Log.d("Base64 Image", selectedImageBase64);

                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        apiService = ApiUtils.createService(ApiService.class);

        productNameEditText = findViewById(R.id.product_name_edit_text);
        productCategorySpinner = findViewById(R.id.product_category_spinner);
        productSupplierSpinner = findViewById(R.id.product_supplier_spinner);
        productPriceEditText = findViewById(R.id.product_price_edit_text);
        productStockEditText = findViewById(R.id.product_stock_edit_text);
        productDescriptionEditText = findViewById(R.id.product_description_edit_text);
        productImageView = findViewById(R.id.product_image_view);
        productActiveSwitch = findViewById(R.id.product_active_switch);
        addProductButton = findViewById(R.id.add_product_button);
        chooseImageButton = findViewById(R.id.choose_image_button);
        takePhotoButton = findViewById(R.id.take_photo_button);

        // Log để kiểm tra
        Log.d("ActivityAdd", "productNameEditText: " + productNameEditText);
        Log.d("ActivityAdd", "productCategorySpinner: " + productCategorySpinner);
        Log.d("ActivityAdd", "productSupplierEditText: " + productSupplierEditText);
        Log.d("ActivityAdd", "productPriceEditText: " + productPriceEditText);
        Log.d("ActivityAdd", "productStockEditText: " + productStockEditText);
        Log.d("ActivityAdd", "productDescriptionEditText: " + productDescriptionEditText);
        Log.d("ActivityAdd", "productActiveSwitch: " + productActiveSwitch);
        Log.d("ActivityAdd", "addProductButton: " + addProductButton);
        Log.d("ActivityAdd", "chooseImageButton: " + chooseImageButton);
        Log.d("ActivityAdd", "takePhotoButton: " + takePhotoButton);

        loadCategories();
        loadSuppliers();

        chooseImageButton.setOnClickListener(v -> chooseImageFromGallery());
        takePhotoButton.setOnClickListener(v -> takePhotoWithCamera());

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
            }
        });
    }

    private void takePhotoWithCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePhotoLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        chooseImageLauncher.launch(intent);
    }

    // Bạn không cần dùng hàm này nữa
    // private String convertBitmapToBase64(Bitmap bitmap) {
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // Giảm quality xuống 80 để giảm dung lượng
    //     byte[] byteArray = outputStream.toByteArray();
    //     String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
    //     Log.d("Base64 Image", base64String); // Log toàn bộ base64 string
    //     return base64String;
    // }

    private void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    categoryList = response.body();
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : categoryList) {
                        categoryNames.add(category.getCategoryName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityAdd.this, android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    productCategorySpinner.setAdapter(adapter);

                    // Thêm lắng nghe sự kiện chọn cho Spinner
                    productCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCategoryId = categoryList.get(position).getCategoryId();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedCategoryId = null;
                        }
                    });
                } else {
                    Toast.makeText(ActivityAdd.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(ActivityAdd.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to load categories", t);
            }
        });
    }

    private void loadSuppliers() {
        apiService.getAllSuppliers().enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful()) {
                    supplierList = response.body();
                    List<String> supplierNames = new ArrayList<>();
                    for (Supplier supplier : supplierList) {
                        supplierNames.add(supplier.getSupplierName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityAdd.this, android.R.layout.simple_spinner_item, supplierNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    productSupplierSpinner.setAdapter(adapter);

                    // Thêm lắng nghe sự kiện chọn cho Spinner
                    productSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedSupplierId = supplierList.get(position).getSupplierId();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedSupplierId = null;
                        }
                    });

                } else {
                    Toast.makeText(ActivityAdd.this, "Failed to load suppliers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Toast.makeText(ActivityAdd.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to load suppliers", t);
            }
        });
    }
    private void addProduct() {
        // Lấy giá trị và trim()
        String productName = productNameEditText.getText().toString().trim();
        String productDescription = productDescriptionEditText.getText().toString().trim();
        String productPrice = productPriceEditText.getText().toString().trim();
        String productStock = productStockEditText.getText().toString().trim();
        boolean productActive = productActiveSwitch.isChecked();

        // Validate input - bao gồm cả kiểm tra selectedCategoryId và selectedSupplierId
        if (productName.isEmpty() || selectedCategoryId == null || selectedSupplierId == null || productPrice.isEmpty() ||
                productStock.isEmpty() || productDescription.isEmpty()) {
            Toast.makeText(ActivityAdd.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi kiểu dữ liệu - sau khi validate
        BigDecimal price;
        int stock;
        try {
            price = new BigDecimal(productPrice);
            stock = Integer.parseInt(productStock);
        } catch (NumberFormatException e) {
            Toast.makeText(ActivityAdd.this, "Giá hoặc số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo CreateProductRequest object
        CreateProductRequest request = new CreateProductRequest();
        request.setName(productName);
        request.setCategoryId(selectedCategoryId);
        request.setSupplierId(selectedSupplierId);
        request.setPrice(price);
        request.setUnitsInStock(stock);
        request.setDescription(productDescription);
        // Không set image ở đây
        // request.setImages(Arrays.asList(selectedImageBase64));
        request.setActive(productActive);

        // Log request object để kiểm tra
        Log.d("CreateProductRequest", request.toString());

        // Upload ảnh trước
        uploadImage(request);
    }
    private void uploadImage(CreateProductRequest productRequest) {
        if (selectedImageBitmap == null) {
            Toast.makeText(ActivityAdd.this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resize ảnh (nếu cần)
        // Bitmap resizedBitmap = resizeBitmap(selectedImageBitmap, 800); // Kích thước tối đa 800x800

        // Tạo file từ bitmap
        File filesDir = getApplicationContext().getFilesDir();
        File imageFile = new File(filesDir, "image_" + System.currentTimeMillis() + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, os); // Hoặc dùng resizedBitmap
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            return;
        }

        // Tạo RequestBody từ file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile); // Sửa MediaType

        // Tạo MultipartBody.Part từ RequestBody
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        // Gọi API để upload ảnh
        Call<ResponseBody> call = apiService.uploadImage(body);
        call.enqueue(new Callback<ResponseBody>() { // Sửa lại thành ResponseBody
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // Lấy response body dưới dạng String
                        String responseBody = response.body().string();

                        // Parse JSON response
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getString("url");

                        Log.d("ImageUrl", imageUrl);

                        // Thêm URL vào danh sách ảnh
                        List<String> images = new ArrayList<>();
                        images.add(imageUrl);
                        productRequest.setImages(images);

                        // Gọi API để thêm sản phẩm
                        addProductWithImage(productRequest);

                    } catch (IOException | JSONException e) {
                        Log.e("API_Error", "Error parsing response body", e);
                        Toast.makeText(ActivityAdd.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý lỗi
                    Toast.makeText(ActivityAdd.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("API_Error", "Response Code: " + response.code() + ", Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("API_Error", "Error reading response body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ActivityAdd.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to upload image", t);
            }
        });
    }


    private void addProductWithImage(CreateProductRequest request) {
        apiService.addProduct(request).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActivityAdd.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ActivityAdd.this, "Thêm sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("API_Error", "Response Code: " + response.code());
                    try {
                        Log.e("API_Error", "Response Body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("API_Error", "Error reading response body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                Toast.makeText(ActivityAdd.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to add product", t);
            }
        });
    }
}