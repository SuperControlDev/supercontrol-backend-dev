package com.supercontrol.backend.dto;

public class AuthResponse {

    private String userId;
    private String username;
    private String provider;

    public AuthResponse(String userId, String username, String provider) {
        this.userId = userId;
        this.username = username;
        this.provider = provider;
    }

    // 기존 getter 유지
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProvider() {
        return provider;
    }
}
