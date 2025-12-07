package com.supercontrol.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserBalanceResponse {
    private boolean success;
    private String reason;

    private String userId;
    private int balance;
}
