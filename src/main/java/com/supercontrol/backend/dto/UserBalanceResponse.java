package com.supercontrol.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalanceResponse {
    private String userId;
    private int balance;
}
