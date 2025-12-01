package com.supercontrol.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameStartResponse {
    private boolean success;
    private int remainingCoins;
    private String reason;
    private long gameStartTime;
    private int durationSec;
    private long sessionId;
}
