package com.supercontrol.backend.repository;

import com.supercontrol.backend.domain.OAuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthIdentityRepository extends JpaRepository<OAuthIdentity, Long> {

    OAuthIdentity findByProviderTypeAndProviderUserId(String providerType, String providerUserId);
}
