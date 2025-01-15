package com.quangtruong.appbanlinhkien.api;

import com.quangtruong.appbanlinhkien.dto.ProductDTO;
import com.quangtruong.appbanlinhkien.dto.SupplierDTO;
import com.quangtruong.appbanlinhkien.model.AuthResponse;
import com.quangtruong.appbanlinhkien.model.Category;
import com.quangtruong.appbanlinhkien.model.Customer;
import com.quangtruong.appbanlinhkien.model.LoginRequest;
import com.quangtruong.appbanlinhkien.model.Product;
import com.quangtruong.appbanlinhkien.model.Supplier;
import com.quangtruong.appbanlinhkien.request.CreateProductRequest;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.quangtruong.appbanlinhkien.dto.CategoryDTO;

public interface ApiService {
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);


    @POST("api/auth/employee/login")
    Call<AuthResponse> loginEmployee(@Body LoginRequest loginRequest);

    @GET("api/products")
    Call<List<Product>> getAllProducts();

    @Multipart
    @POST("api/upload/image")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    @POST("api/admin/products/add")
    Call<ProductDTO> addProduct(@Body CreateProductRequest product);

    @GET("api/categories")
    Call<List<CategoryDTO>> getAllCategories();

    @GET("api/suppliers")
    Call<List<Supplier>> getAllSuppliers();

    @GET("api/admin/products/exists")
    Call<Boolean> checkProductNameExists(@Query("name") String productName);

    @GET("api/admin/products/{id}")
    Call<ProductDTO> getProduct(@Path("id") Long productId);

    @PUT("api/admin/products/{id}")
    Call<ProductDTO> updateProduct(@Path("id") Long productId, @Body CreateProductRequest request);

    @DELETE("api/admin/products/{id}")
    Call<Void> deleteProduct(@Path("id") Long productId);

    //Category
    @POST("api/admin/categories/add")
    Call<CategoryDTO> createCategory(@Body Category category);

    @PUT("api/admin/categories/{id}")
    Call<CategoryDTO> updateCategory(@Path("id") Long id, @Body Category category);

    @DELETE("api/admin/categories/{id}")
    Call<Void> deleteCategory(@Path("id") Long id);

    @GET("api/admin/categories/{id}")
    Call<CategoryDTO> getCategoryById(@Path("id") Long categoryId);

    // Supplier
    @GET("api/admin/suppliers")
    Call<List<SupplierDTO>> getAllAdminSuppliers();

    @GET("api/admin/suppliers/{id}")
    Call<SupplierDTO> getSupplierById(@Path("id") Long supplierId);

    @POST("api/admin/suppliers/add")
    Call<SupplierDTO> createSupplier(@Body Supplier supplier);

    @PUT("api/admin/suppliers/{id}")
    Call<SupplierDTO> updateSupplier(@Path("id") Long id, @Body Supplier supplier);

    @DELETE("api/admin/suppliers/{id}")
    Call<Void> deleteSupplier(@Path("id") Long id);

}