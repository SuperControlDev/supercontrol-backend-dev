package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.OAuthIdentity;
import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.dto.AuthResponse;
import com.supercontrol.backend.repository.OAuthIdentityRepository;
import com.supercontrol.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthIdentityRepository oauthRepository;
    private final UserRepository userRepository;

    @Transactional
    public AuthResponse login(String provider, String providerUserId, String username) {

        // 1️⃣ 기존 OAuth 사용자 조회 (null 가능)
        OAuthIdentity oauth = oauthRepository.findByProviderTypeAndProviderUserId(
                provider, providerUserId);

        User user;

        if (oauth != null) {
            // 2️⃣ 기존 사용자
            user = oauth.getUser();
        } else {
            // 3️⃣ 신규 사용자 생성
            String userId = generateUserId(provider, providerUserId);

            user = new User();
            user.setUserId(userId); // ⭐⭐⭐ 반드시 필요
            // user.setUsername(provider + " User");
            user.setUsername(
                    username != null && !username.isBlank()
                            ? username
                            : provider + " User");
            user.setProvider(provider);
            user.setBalance(0);
            user.setFreeTickets(5);
            user.setCountryCode("KR");

            userRepository.save(user);

            // 4️⃣ OAuthIdentity 생성
            OAuthIdentity newOauth = new OAuthIdentity();
            newOauth.setProviderType(provider);
            newOauth.setProviderUserId(providerUserId);
            newOauth.setUser(user);

            oauthRepository.save(newOauth);
        }

        // 5️⃣ 프론트 응답
        return new AuthResponse(
                user.getUserId(),
                user.getUsername(),
                user.getProvider(),
                user.getBalance(),
                user.getFreeTickets());
    }

    /**
     * providerUserId가 이미 google_xxx 형태로 오는 경우 중복 방지
     */
    private String generateUserId(String provider, String providerUserId) {
        if (providerUserId.startsWith(provider + "_")) {
            return providerUserId;
        }
        return provider + "_" + providerUserId;
    }
}
