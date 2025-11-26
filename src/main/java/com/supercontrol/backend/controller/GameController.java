package com.supercontrol.backend.controller;

import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @PostMapping("/start")
    public GameStartResponse start(@RequestBody GameStartRequest req) {
        // TODO: 코인 차감 & 세션 연동
        long now = System.currentTimeMillis();
        int duration = 15;

        return GameStartResponse.builder()
                .success(true)
                .remainingCoins(90) // 임시 값
                .gameStartTime(now)
                .durationSec(duration)
                .build();
    }

    @Getter @Setter
    public static class GameStartRequest {
        private String sessionId;
        private int machineId;
        private String userId;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameStartResponse {
        private boolean success;
        private int remainingCoins;
        private long gameStartTime;
        private int durationSec;
    }
}
