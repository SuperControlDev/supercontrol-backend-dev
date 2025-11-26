package com.supercontrol.backend.controller;

import lombok.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/kakao")
    public LoginResponse kakaoLogin(@RequestBody SocialLoginRequest request) {
        // TODO: 실제 Kakao OAuth 연동 (지금은 토큰만 받아서 mock 유저 리턴)
        return LoginResponse.builder()
                .userId("user-kakao-001")
                .username("Kakao User")
                .accessToken("mock-jwt-token")
                .build();
    }

    @PostMapping("/google")
    public LoginResponse googleLogin(@RequestBody SocialLoginRequest request) {
        return LoginResponse.builder()
                .userId("user-google-001")
                .username("Google User")
                .accessToken("mock-jwt-token")
                .build();
    }

    @PostMapping("/apple")
    public LoginResponse appleLogin(@RequestBody SocialLoginRequest request) {
        return LoginResponse.builder()
                .userId("user-apple-001")
                .username("Apple User")
                .accessToken("mock-jwt-token")
                .build();
    }

    @Getter
    @Setter
    public static class SocialLoginRequest {
        private String token; // 프론트에서 받은 OAuth 토큰
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String userId;
        private String username;
        private String accessToken;
        private String refreshToken;
    }
}
