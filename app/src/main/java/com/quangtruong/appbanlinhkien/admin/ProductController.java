package com.quangtruong.appbanlinhkien.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cloudinary.android.callback.ErrorInfo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.adapter.ImageAdapter;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.dto.ProductDTO;
import com.quangtruong.appbanlinhkien.model.Category;
import com.quangtruong.appbanlinhkien.model.Supplier;
import com.quangtruong.appbanlinhkien.request.CreateProductRequest;
import com.quangtruong.appbanlinhkien.ui.product.ProductViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductController extends AppCompatActivity implements ImageAdapter.ImageRemoveListener {

    private static final int PICK_IMAGES_REQUEST = 2;
    private static final int MAX_IMAGE_SIZE = 800;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private TextInputEditText productNameEditText, productPriceEditText, productStockEditText, productDescriptionEditText;
    private Spinner productCategorySpinner, productSupplierSpinner;
    private SwitchMaterial productActiveSwitch;
    private Button updateProductButton, chooseImageButton, takePhotoButton;
    private ApiService apiService;
    private List<Category> categoryList;
    private List<Supplier> supplierList;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private RecyclerView imagesRecyclerView;
    private Cloudinary cloudinary;
    private Map<String, Long> categoryMap = new HashMap<>();
    private Map<String, Long> supplierMap = new HashMap<>();
    private Long selectedCategoryId;
    private Long selectedSupplierId;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ProductViewModel viewModel;
    private Long productId;
    private CountDownLatch loadDataLatch;
    private ActivityResultLauncher<Intent> chooseImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_form);
        // Khởi tạo Cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "YOUR_CLOUD_NAME");
        config.put("api_key", "YOUR_API_KEY");
        config.put("api_secret", "YOUR_API_SECRET");
        MediaManager.init(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        apiService = new ApiUtils(this).createService(ApiService.class);
        // Ánh xạ các view
        productNameEditText = findViewById(R.id.product_name_edit_text);
        productCategorySpinner = findViewById(R.id.product_category_spinner);
        productSupplierSpinner = findViewById(R.id.product_supplier_spinner);
        productPriceEditText = findViewById(R.id.product_price_edit_text);
        productStockEditText = findViewById(R.id.product_stock_edit_text);
        productDescriptionEditText = findViewById(R.id.product_description_edit_text);
        productActiveSwitch = findViewById(R.id.product_active_switch);
        updateProductButton = findViewById(R.id.update_product_button);
        progressBar = findViewById(R.id.progress_bar);
        imagesRecyclerView = findViewById(R.id.product_images_recycler_view);

        chooseImageButton = findViewById(R.id.choose_image_button);
        takePhotoButton = findViewById(R.id.take_photo_button);

        imageAdapter = new ImageAdapter(selectedImageUris, this, this);
        imagesRecyclerView.setAdapter(imageAdapter);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        loadDataLatch = new CountDownLatch(2);
        loadCategories();
        loadSuppliers();

        // Lấy productId từ Intent
        productId = getIntent().getLongExtra("PRODUCT_ID", -1);
        // Log product id
        Log.d("ActivityEdit", "productId: " + productId);
        if (productId == -1) {
            getSupportActionBar().setTitle("Thêm sản phẩm");
        } else {
            getSupportActionBar().setTitle("Cập nhật sản phẩm");
            // Load thông tin sản phẩm nếu là sửa
            new Thread(() -> {
                try {
                    loadDataLatch.await();
                    handler.post(() -> loadProductDetails(productId));
                } catch (InterruptedException e) {
                    Log.e("ProductController", "Error waiting for data load", e);
                }
            }).start();
        }

        // Xử lý sự kiện click nút "Cập nhật" hoặc "thêm mới"
        updateProductButton.setOnClickListener(v -> {
            if (productId == -1) {
                addProduct();
            } else {
                updateProduct(productId);
            }
        });

        chooseImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                for (int i = 0; i < count; i++) {
                                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                    selectedImageUris.add(imageUri);
                                    imageAdapter.notifyItemInserted(selectedImageUris.size() - 1);
                                }
                            } else if (data.getData() != null) {
                                Uri imageUri = data.getData();
                                selectedImageUris.add(imageUri);
                                imageAdapter.notifyItemInserted(selectedImageUris.size() - 1);
                            }
                        }
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        // Chuyển Bitmap thành Uri và thêm vào danh sách
                        Uri imageUri = getImageUri(imageBitmap);
                        selectedImageUris.add(imageUri);
                        imageAdapter.notifyItemInserted(selectedImageUris.size() - 1);
                    }
                }
        );
        chooseImageButton.setOnClickListener(v -> chooseImageFromGallery());
        takePhotoButton.setOnClickListener(v -> takePhotoWithCamera());
    }

    private Uri getImageUri(Bitmap inImage) {
        File tempDir = getFilesDir(); // Lấy thư mục lưu trữ nội bộ của ứng dụng
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/"); // Thêm thư mục con .temp (nếu muốn)
        tempDir.mkdir(); // Tạo thư mục nếu chưa tồn tại
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tempImage", ".jpg", tempDir); // Tạo file tạm
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] bitmapData = bytes.toByteArray();

            // Ghi dữ liệu vào file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
        }

        return Uri.fromFile(tempFile);
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
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePhotoLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều ảnh
        chooseImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void loadProductDetails(Long productId) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getProduct(productId).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ProductDTO product = response.body();
                    // Hiển thị thông tin sản phẩm lên các view
                    productNameEditText.setText(product.getProductName());

                    // Tìm vị trí của category và supplier trong danh sách
                    int categoryPosition = -1;
                    for (int i = 0; i < categoryList.size(); i++) {
                        if (categoryList.get(i).getCategoryId().equals(product.getCategoryId())) {
                            categoryPosition = i;
                            break;
                        }
                    }
                    if (categoryPosition != -1) {
                        productCategorySpinner.setSelection(categoryPosition);
                    }

                    int supplierPosition = -1;
                    for (int i = 0; i < supplierList.size(); i++) {
                        if (supplierList.get(i).getSupplierId().equals(product.getSupplierId())) {
                            supplierPosition = i;
                            break;
                        }
                    }
                    if (supplierPosition != -1) {
                        productSupplierSpinner.setSelection(supplierPosition);
                    }

                    productPriceEditText.setText(product.getUnitPrice().toString());
                    productStockEditText.setText(String.valueOf(product.getUnitsInStock()));
                    productDescriptionEditText.setText(product.getDescription());
                    productActiveSwitch.setChecked(product.isActive());

                    // Hiển thị hình ảnh (nếu có)
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        selectedImageUris.clear();
                        for (String imageUrl : product.getImages()) {
                            selectedImageUris.add(Uri.parse(imageUrl));
                        }
                        imageAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ProductController.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductController.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_Error", "Failed to load product details", t);
            }
        });
    }

    private void updateProduct(Long productId) {
        // Lấy thông tin từ các view
        String productName = productNameEditText.getText().toString().trim();

        // Lấy categoryName từ Spinner
        String categoryName = productCategorySpinner.getSelectedItem().toString();
        Long categoryId = null;
        // Tìm categoryId dựa vào categoryName
        for (Category category : categoryList) {
            if (category.getCategoryName().equals(categoryName)) {
                categoryId = category.getCategoryId();
                break;
            }
        }

        // Lấy supplierName từ Spinner
        String supplierName = productSupplierSpinner.getSelectedItem().toString();
        Long supplierId = null;
        for(Supplier supplier : supplierList){
            if(supplier.getSupplierName().equals(supplierName)){
                supplierId = supplier.getSupplierId();
                break;
            }
        }

        String priceString = productPriceEditText.getText().toString().trim();
        String stockString = productStockEditText.getText().toString().trim();

        String productDescription = productDescriptionEditText.getText().toString().trim();
        boolean productActive = productActiveSwitch.isChecked();

        // Kiểm tra giá trị null hoặc rỗng trước khi chuyển đổi kiểu
        if (productName.isEmpty() || categoryId == null || supplierId == null || priceString.isEmpty() ||
                stockString.isEmpty() || productDescription.isEmpty()) {
            Toast.makeText(ProductController.this, R.string.please_enter_all_information, Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal productPrice;
        int productStock;
        try {
            productPrice = new BigDecimal(priceString);
            productStock = Integer.parseInt(stockString);
        } catch (NumberFormatException e) {
            Toast.makeText(ProductController.this, R.string.invalid_price_or_stock, Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request object
        CreateProductRequest request = new CreateProductRequest();
        request.setName(productName);
        request.setCategoryId(categoryId);
        request.setSupplierId(supplierId);
        request.setPrice(productPrice);
        request.setUnitsInStock(productStock);
        request.setDescription(productDescription);
        request.setActive(productActive);
        Log.d("ActivityEdit", "productActiveSwitch.isChecked(): " + productActiveSwitch.isChecked());
        Log.d("ActivityEdit", "updateProduct request: " + request.toString());
        Gson gson = new Gson();
        Log.d("ActivityEdit", "updateProduct request: " + gson.toJson(request));

        // Gọi API để cập nhật sản phẩm
        progressBar.setVisibility(View.VISIBLE);
        apiService.updateProduct(productId, request).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductController.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProductController.this, "Cập nhật sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductController.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    categoryList = response.body();
                    List<String> categoryNames = new ArrayList<>();
                    categoryMap.clear(); // Xóa dữ liệu cũ trong Map
                    for (Category category : categoryList) {
                        categoryNames.add(category.getCategoryName());
                        categoryMap.put(category.getCategoryName(), category.getCategoryId()); // Thêm vào Map
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ProductController.this, android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    productCategorySpinner.setAdapter(adapter);

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
                    loadDataLatch.countDown();
                } else {
                    handler.post(() -> Toast.makeText(ProductController.this, R.string.failed_to_load_categories, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                handler.post(() -> Toast.makeText(ProductController.this, getString(R.string.error) + t.getMessage(), Toast.LENGTH_SHORT).show());
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
                    supplierMap.clear(); // Xóa dữ liệu cũ trong Map
                    for (Supplier supplier : supplierList) {
                        supplierNames.add(supplier.getSupplierName());
                        supplierMap.put(supplier.getSupplierName(), supplier.getSupplierId()); // Thêm vào Map
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ProductController.this, android.R.layout.simple_spinner_item, supplierNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    productSupplierSpinner.setAdapter(adapter);

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
                    loadDataLatch.countDown();
                } else {
                    handler.post(() -> Toast.makeText(ProductController.this, R.string.failed_to_load_suppliers, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                handler.post(() -> Toast.makeText(ProductController.this, getString(R.string.error) + t.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("API_Error", "Failed to load suppliers", t);
            }
        });
    }
    @Override
    public void onImageRemove(int position) {
        selectedImageUris.remove(position);
        imageAdapter.notifyItemRemoved(position);
    }


    private void addProduct() {
        String productName = productNameEditText.getText().toString().trim();
        Long categoryId = selectedCategoryId;
        Long supplierId = selectedSupplierId;
        String productPrice = productPriceEditText.getText().toString().trim();
        String productStock = productStockEditText.getText().toString().trim();
        String productDescription = productDescriptionEditText.getText().toString().trim();
        boolean productActive = productActiveSwitch.isChecked();

        if (productName.isEmpty() || categoryId == null || supplierId == null || productPrice.isEmpty() || productStock.isEmpty() || productDescription.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_all_information, Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal price;
        int stock;
        try {
            price = new BigDecimal(productPrice);
            stock = Integer.parseInt(productStock);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_price_or_stock, Toast.LENGTH_SHORT).show();
            return;
        }

        CreateProductRequest request = new CreateProductRequest();
        request.setName(productName);
        request.setCategoryId(categoryId);
        request.setSupplierId(supplierId);
        request.setPrice(price);
        request.setUnitsInStock(stock);
        request.setDescription(productDescription);
        request.setActive(productActive);

        if (selectedImageUris != null && !selectedImageUris.isEmpty()) {
            uploadImages(request, productId);
        } else {
            addProductToDatabase(request);
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void uploadImages(CreateProductRequest productRequest, Long productId) {
        if (selectedImageUris.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            if (productId == null) {
                addProductToDatabase(productRequest);
            } else {
                updateProductAndImages(productId, productRequest);
            }
            return;
        }

        List<String> imageUrls = new ArrayList<>();
        final int totalImages = selectedImageUris.size();
        final int[] uploadedImages = {0};

        for (Uri imageUri : selectedImageUris) {
            // Tạo background thread để upload ảnh
            new Thread(() -> {
                try {
                    // Mở InputStream từ Uri
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    // Upload ảnh lên Cloudinary sử dụng MediaManager
                    String publicId = "product_" + System.currentTimeMillis(); // Tạo public ID cho ảnh
                    MediaManager.get().upload(imageUri)
                            .option("public_id", publicId)
                            .option("folder", "product_images/") // Thư mục trên Cloudinary (tùy chọn)
                            .option("resource_type", "image")
                            .callback(new UploadCallback() { // Thay đổi UploadResultCallback thành UploadCallback
                                @Override
                                public void onStart(String requestId) {
                                    // Bắt đầu upload
                                    Log.d("Upload", "Upload started for requestId: " + requestId);
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                    // Tiến trình upload
                                    Double progress = (double) bytes / totalBytes;
                                    Log.d("Upload", "Upload progress for requestId: " + requestId + ", progress: " + progress);
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    // Upload thành công
                                    String imageUrl = (String) resultData.get("secure_url");
                                    imageUrls.add(imageUrl);
                                    uploadedImages[0]++;

                                    // Nếu đã upload hết ảnh
                                    if (uploadedImages[0] == totalImages) {
                                        handler.post(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            productRequest.setImages(imageUrls);
                                            if (productId == null) {
                                                addProductToDatabase(productRequest);
                                            } else {
                                                updateProductAndImages(productId, productRequest);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    // Lỗi khi upload
                                    Log.e("Upload", "Error uploading image for requestId: " + requestId + ", error: " + error.getDescription());
                                    handler.post(() -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ProductController.this, "Lỗi khi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {
                                    // Upload được lên lịch lại (ít khi xảy ra)
                                    Log.d("Upload", "Upload rescheduled for requestId: " + requestId + ", error: " + error.getDescription());
                                }
                            })
                            .dispatch(); // Bắt đầu upload

                } catch (IOException e) {
                    Log.e("UploadImages", "Failed to open InputStream", e);
                    handler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProductController.this, "Lỗi khi mở ảnh", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start(); // Bắt đầu background thread
        }
    }

    private void updateProductAndImages(Long productId, CreateProductRequest request) {
        apiService.updateProduct(productId, request).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductController.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProductController.this, "Cập nhật sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("API_Error", "Response Code: " + response.code() + ", Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("API_Error", "Error reading response body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductController.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductToDatabase(CreateProductRequest request) {
        apiService.addProduct(request).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductController.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProductController.this, "Thêm sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductController.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkProductNameExists(String productName, Runnable onComplete) {

    }
}