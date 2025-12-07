package com.supercontrol.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String userName;
    // private String email;
    // private String nickname;
    private int balance;
    private LocalDateTime createdAt;
}