package com.supercontrol.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameStartResponse {
    private boolean success;

    /**
     * 기존 호환용(legacy)
     * - 예전 응답 포맷에서 쓰던 필드 유지
     */
    private int remainingCoins;

    /**
     * 현재 정책: free_ticket 차감
     */
    private int remainingFreeTickets;

    private String reason;
    private long gameStartTime;
    private int durationSec;

    /**
     * ✅ 외부(FE/End API)가 사용하는 세션 ID는 "Redis 세션 ID"로 통일
     * 예: "550e8400-e29b-41d4-a716-446655440000"
     */
    private String sessionId;

    /**
     * ✅ DB 저장용 PK가 필요하면 별도로 제공(현 기능 최대 유지)
     * 예: 13
     */
    private Long dbSessionId;
}
