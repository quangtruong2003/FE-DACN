package com.quangtruong.appbanlinhkien.api;

import com.quangtruong.appbanlinhkien.dto.ProductDTO;
import com.quangtruong.appbanlinhkien.model.AuthResponse;
import com.quangtruong.appbanlinhkien.model.Category;
import com.quangtruong.appbanlinhkien.model.LoginRequest;
import com.quangtruong.appbanlinhkien.model.Product;
import com.quangtruong.appbanlinhkien.model.Supplier;
import com.quangtruong.appbanlinhkien.request.CreateProductRequest;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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
    Call<List<Category>> getAllCategories();

    @GET("api/suppliers")
    Call<List<Supplier>> getAllSuppliers();

}