package com.quangtruong.appbanlinhkien.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class ProductDTO implements Serializable {
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

    // Constructors
    public ProductDTO() {}

    public ProductDTO(Long productId, String productName, Long categoryId, String categoryName,
                      Long supplierId, String supplierName, BigDecimal unitPrice,
                      int unitsInStock, String description, List<String> images, boolean active) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.unitPrice = unitPrice;
        this.unitsInStock = unitsInStock;
        this.description = description;
        this.images = images;
        this.active = active;
    }

    // Getters and Setters (bạn có thể dùng Lombok để tự động generate)

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

}