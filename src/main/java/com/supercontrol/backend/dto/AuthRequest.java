package com.supercontrol.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String providerUserId; // FE가 보내는 고유 ID
}
