package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.dto.UserBalanceResponse;
import com.supercontrol.backend.repository.UserRepository;
import com.supercontrol.backend.dto.UserProfileResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserBalanceResponse getBalance(String userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return UserBalanceResponse.builder()
                    .success(false)
                    .reason("USER_NOT_FOUND")
                    .build();
        }

        return UserBalanceResponse.builder()
                .success(true)
                .userId(user.getUserId())
                .balance(user.getBalance())
                .build();
    }

    // // JWT 인증된 사용자의 ID 가져오기
    // public Long getCurrentUserId() {
    // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    // CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    // return user.getId();
    // }

    // public UserProfileResponse getUserProfile(String userId) {
    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new RuntimeException("User not found"));

    // return new UserProfileResponse(
    // user.getUserId(),
    // user.getUsername(),
    // // user.getEmail(),
    // // user.getNickname(),
    // user.getBalance(),
    // user.getCreatedAt());
    // }

    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                // user.getEmail(),
                // user.getNickname(),
                user.getBalance(),
                user.getCreatedAt());
    }

}
