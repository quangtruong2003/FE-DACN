package com.quangtruong.appbanlinhkien.model;

public class AuthResponse {
    private String message;
    private String jwt;

    // Constructor, getters, setters
    public AuthResponse(String message, String jwt) {
        this.message = message;
        this.jwt = jwt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
