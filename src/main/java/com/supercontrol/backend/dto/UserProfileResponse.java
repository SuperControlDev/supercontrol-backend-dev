package com.supercontrol.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String userName;
    private int balance;
    private LocalDateTime createdAt;
    private int free_tickets;
    private int paid_tickets;
    private String country_coude;
}