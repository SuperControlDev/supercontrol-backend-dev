package com.supercontrol.backend.controller;

import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.dto.AuthRequest;
import com.supercontrol.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/{provider}")
    public User login(
            @PathVariable("provider") String provider,
            @RequestBody AuthRequest request) {
        return authService.login(provider, request);
    }
}
