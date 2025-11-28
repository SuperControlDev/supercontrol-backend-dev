package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.OAuthIdentity;
import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.dto.AuthRequest;
import com.supercontrol.backend.repository.OAuthIdentityRepository;
import com.supercontrol.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthIdentityRepository oauthIdentityRepository;

    public User login(String provider, AuthRequest request) {

        String providerType = provider.toLowerCase();
        String providerUserId = request.getProviderUserId();

        // 1) OAuthIdentity 조회 (이미 가입된 사용자)
        OAuthIdentity identity = oauthIdentityRepository.findByProviderTypeAndProviderUserId(providerType,
                providerUserId);

        if (identity != null) {
            return identity.getUser(); // 기존 회원
        }

        // 2) 신규 User 생성
        User newUser = User.builder()
                .username(providerType + "_" + providerUserId)
                .provider(providerType)
                .balance(0)
                .build();
        userRepository.save(newUser);

        // 3) OAuthIdentity 연결 저장
        OAuthIdentity newIdentity = OAuthIdentity.builder()
                .user(newUser)
                .providerType(providerType)
                .providerUserId(providerUserId)
                .build();
        oauthIdentityRepository.save(newIdentity);

        return newUser;
    }
}
