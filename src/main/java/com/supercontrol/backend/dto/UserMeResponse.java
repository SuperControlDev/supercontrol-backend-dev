package com.supercontrol.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMeResponse {
    private String userId;
    private String username;
    private int balance;
    private String avatar;
    private boolean isLoggedIn;
}
