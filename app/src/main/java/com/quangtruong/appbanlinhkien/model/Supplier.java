package com.quangtruong.appbanlinhkien.model;

public class Supplier {
    private Long supplierId;
    private String supplierName;

    // Constructors
    public Supplier() {
    }

    public Supplier(Long supplierId, String supplierName) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }

    // Getters and Setters
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
