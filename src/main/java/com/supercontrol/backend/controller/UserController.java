package com.supercontrol.backend.controller;

import com.supercontrol.backend.dto.UserBalanceResponse;
import com.supercontrol.backend.dto.UserMeResponse;
import com.supercontrol.backend.dto.UserProfileResponse;
import com.supercontrol.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /**
     * 사용자 잔액 조회
     * GET /api/user/balance?userId=xxxxx
     */
    @GetMapping("/balance")
    public ResponseEntity<UserBalanceResponse> getBalance(@RequestParam String userId) {
        return ResponseEntity.ok(userService.getBalance(userId));
    }

    /**
     * 사용자 프로필 조회
     * GET /api/user/me?userId=xxxxx
     */
    @GetMapping("/me")
    public UserMeResponse me(@RequestParam String userId) {
        return UserMeResponse.builder()
                .userId(userId)
                .username("Kakao User") // 임시 mock
                .balance(100) // 임시 고정값
                .isLoggedIn(true)
                .build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestParam String userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
