package com.quangtruong.appbanlinhkien.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class Product implements Serializable {
    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal unitPrice;
    private int unitsInStock;
    private String description;
    private List<String> images;
    private boolean active;

    // Constructors, Getters, and Setters

    public Product() {
    }

    public Product(Long productId, String productName, Long categoryId, Long supplierId, String categoryName, String supplierName, BigDecimal unitPrice, int unitsInStock, String description, List<String> images, boolean active) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.categoryName = categoryName;
        this.supplierName = supplierName;
        this.unitPrice = unitPrice;
        this.unitsInStock = unitsInStock;
        this.description = description;
        this.images = images;
        this.active = active;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getUnitsInStock() {
        return unitsInStock;
    }

    public void setUnitsInStock(int unitsInStock) {
        this.unitsInStock = unitsInStock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        active = active;
    }
}