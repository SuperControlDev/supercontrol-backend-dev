package com.supercontrol.backend.controller;

import com.supercontrol.backend.dto.UserBalanceResponse;
import com.supercontrol.backend.dto.UserMeResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    // 아직 인증/JWT 안 붙였으므로 임시 mock
    private static final String MOCK_USER_ID = "user-kakao-001";

    @GetMapping("/me")
    public UserMeResponse me() {
        return UserMeResponse.builder()
                .userId(MOCK_USER_ID)
                .username("Kakao User")
                .balance(100) // 100코인
                .avatar(null)
                .isLoggedIn(true)
                .build();
    }

    @GetMapping("/balance")
    public UserBalanceResponse balance() {
        return UserBalanceResponse.builder()
                .userId(MOCK_USER_ID)
                .balance(100)
                .build();
    }
}
