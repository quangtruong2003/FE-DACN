package com.quangtruong.appbanlinhkien.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils {

    public static final String BASE_URL = "http://10.0.2.2:8080/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(createGsonConverterFactory());
    private static Retrofit retrofit = builder.build();
    private static Context context;

    // Thêm static vào đây
    private static ApiService apiService;

    public ApiUtils(Context context) {
        this.context = context;
    }
    private static GsonConverterFactory createGsonConverterFactory() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Đăng ký bộ chuyển đổi cho LocalDateTime
        Gson gson = Converters.registerLocalDateTime(gsonBuilder).create();
        return GsonConverterFactory.create(gson);
    }
    public static <S> S createService(Class<S> serviceClass) {
        // Lấy token từ SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("token", null);

        if (authToken != null) {
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor("Bearer " + authToken);

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        return retrofit.create(serviceClass);
    }

    // Thêm phương thức updateToken
    public static void updateToken(Context context, String newToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", newToken);
        editor.apply();

        // Cập nhật lại ApiService với token mới
        apiService = new ApiUtils(context).createService(ApiService.class);
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = new ApiUtils(context).createService(ApiService.class);
        }
        return apiService;
    }
}