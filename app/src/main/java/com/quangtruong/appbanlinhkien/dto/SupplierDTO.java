package com.quangtruong.appbanlinhkien.dto;

import java.io.Serializable;

public class SupplierDTO implements Serializable {
    private Long supplierId;
    private String supplierName;
    private String contactName;
    private String address;
    private String phone;
    private String email;
    private String website;

    // Constructors
    public SupplierDTO() {
    }

    public SupplierDTO(Long supplierId, String supplierName, String contactName, String address, String phone, String email, String website) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactName = contactName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.website = website;
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}