package com.supercontrol.backend.dto;

public class AuthResponse {

    private String userId;
    private String username;
    private String provider;
    private int balance;
    private int freeTickets;

    public AuthResponse(String userId, String username, String provider, int balance, int freeTickets) {
        this.userId = userId;
        this.username = username;
        this.provider = provider;
        this.balance = balance;
        this.freeTickets = freeTickets;
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

    public int getBalance() {
        return balance;
    }

    public int getFreeTickets() {
        return freeTickets;
    }
}
