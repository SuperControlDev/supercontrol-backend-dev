package com.supercontrol.backend.controller;

import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.dto.AuthRequest;
import com.supercontrol.backend.dto.AuthResponse;
import com.supercontrol.backend.dto.OAuthLoginRequest;
import com.supercontrol.backend.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/{provider}")
    public ResponseEntity<AuthResponse> oauthLogin(
            @PathVariable String provider,
            @RequestBody OAuthLoginRequest request) {
        AuthResponse response = authService.login(provider, request.getProviderUserId());

        return ResponseEntity.ok(response);
    }

}
