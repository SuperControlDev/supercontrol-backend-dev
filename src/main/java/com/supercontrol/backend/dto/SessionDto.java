package com.supercontrol.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private String sessionId;
    private int machineId;
    private String userId;
    private long startAt;
    private long expiresAt;
    private String status;      // active / ended / expired / failed
    private int durationSec;
}
