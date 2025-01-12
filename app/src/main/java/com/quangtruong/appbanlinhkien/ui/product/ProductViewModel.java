package com.quangtruong.appbanlinhkien.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.quangtruong.appbanlinhkien.model.Category;
import com.quangtruong.appbanlinhkien.model.Supplier;

import java.util.List;

public class ProductViewModel extends ViewModel {

    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private MutableLiveData<List<Supplier>> suppliers = new MutableLiveData<>();
    // Thêm các LiveData khác nếu cần

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories.setValue(categories);
    }

    public LiveData<List<Supplier>> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers.setValue(suppliers);
    }

    // Thêm các phương thức để load data từ API, upload ảnh, ...
}