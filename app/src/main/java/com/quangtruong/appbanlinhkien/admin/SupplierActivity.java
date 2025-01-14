package com.quangtruong.appbanlinhkien.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quangtruong.appbanlinhkien.R;
import com.quangtruong.appbanlinhkien.adapter.SupplierAdapter;
import com.quangtruong.appbanlinhkien.api.ApiService;
import com.quangtruong.appbanlinhkien.api.ApiUtils;
import com.quangtruong.appbanlinhkien.dto.SupplierDTO;
import com.quangtruong.appbanlinhkien.model.Supplier;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupplierActivity extends AppCompatActivity implements SupplierAdapter.SupplierClickListener {

    private RecyclerView supplierRecyclerView;
    private SupplierAdapter supplierAdapter;
    private List<SupplierDTO> supplierList;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddSupplier;
    private ActivityResultLauncher<Intent> addSupplierLauncher;
    private ActivityResultLauncher<Intent> editSupplierLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        supplierRecyclerView = findViewById(R.id.supplier_list);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        fabAddSupplier = findViewById(R.id.fab_add_supplier);

        apiService = new ApiUtils(this).createService(ApiService.class);

        supplierList = new ArrayList<>();
        supplierAdapter = new SupplierAdapter(supplierList, this);
        supplierRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        supplierRecyclerView.setAdapter(supplierAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadSuppliers);

        addSupplierLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadSuppliers();
                    }
                });

        editSupplierLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadSuppliers();
                    }
                }
        );

        fabAddSupplier.setOnClickListener(v -> {
            Intent intent = new Intent(SupplierActivity.this, AddEditSupplierActivity.class);
            intent.setAction(AddEditSupplierActivity.ACTION_ADD);
            addSupplierLauncher.launch(intent);
        });

        loadSuppliers();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (supplierAdapter != null) {
                    // Chỉ filter khi newText không rỗng
                    if (!newText.isEmpty()) {
                        supplierAdapter.getFilter().filter(newText);
                    } else {
                        // Nếu newText rỗng, hiển thị lại toàn bộ danh sách
                        supplierAdapter.setSuppliers(supplierList);
                    }
                }
                return true;
            }
        });

        return true;
    }

    private void loadSuppliers() {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getAllAdminSuppliers().enqueue(new Callback<List<SupplierDTO>>() {
            @Override
            public void onResponse(Call<List<SupplierDTO>> call, Response<List<SupplierDTO>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    supplierList.clear();
                    List<SupplierDTO> suppliers = response.body();
                    if (suppliers != null) {
                        supplierList.addAll(suppliers);
                    }
                    supplierAdapter.setSuppliers(supplierList);
                    supplierAdapter.notifyDataSetChanged();
                    Log.d("SupplierActivity", "Loaded " + supplierList.size() + " suppliers");
                } else {
                    Toast.makeText(SupplierActivity.this, "Failed to load suppliers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SupplierDTO>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(SupplierActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SupplierActivity", "Error loading suppliers", t);
            }
        });
    }

    @Override
    public void onSupplierClick(SupplierDTO supplier) {
        Intent intent = new Intent(this, AddEditSupplierActivity.class);
        intent.setAction(AddEditSupplierActivity.ACTION_EDIT);
        intent.putExtra(AddEditSupplierActivity.EXTRA_SUPPLIER_ID, supplier.getSupplierId());
        editSupplierLauncher.launch(intent);
    }

    @Override
    public void onSupplierLongClick(SupplierDTO supplier) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhà cung cấp")
                .setMessage("Bạn có chắc chắn muốn xóa nhà cung cấp " + supplier.getSupplierName() + "?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteSupplier(supplier.getSupplierId()))
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteSupplier(Long supplierId) {
        apiService.deleteSupplier(supplierId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SupplierActivity.this, "Supplier deleted successfully", Toast.LENGTH_SHORT).show();
                    loadSuppliers(); // Refresh the list after deletion
                } else {
                    Toast.makeText(SupplierActivity.this, "Failed to delete supplier", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SupplierActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}