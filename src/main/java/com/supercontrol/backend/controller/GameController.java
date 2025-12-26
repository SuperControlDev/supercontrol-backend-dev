package com.supercontrol.backend.controller;

import com.supercontrol.backend.domain.PlaySession;
import com.supercontrol.backend.dto.GameEndRequest;
import com.supercontrol.backend.dto.GameStartRequest;
import com.supercontrol.backend.dto.GameStartResponse;
import com.supercontrol.backend.dto.HeartbeatRequest;
import com.supercontrol.backend.service.GameEndService;
import com.supercontrol.backend.service.GameService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameEndService gameEndService;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/start")
    public ResponseEntity<GameStartResponse> startGame(
            @RequestBody GameStartRequest request) {
        GameStartResponse response = gameService.startGame(request);

        System.out.println("[SuperCon Log] GameController /start 호출됨");
        System.out.println("[SuperCon Log] 유저: " + request.getUserId() + ", 머신: " + request.getMachineId());

        // return gameService.startGame(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 게임 종료 (사용자 / FE 타이머)
     */
    @PostMapping("/end")
    public ResponseEntity<Void> endGame(
            @RequestBody GameEndRequest request) {
        gameEndService.endSession(
                request.getSessionId(),
                request.getReason());
        return ResponseEntity.ok().build();
    }

    /**
     * heartbeat (연결 유지)
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestBody HeartbeatRequest request) {
        // 세션이 살아있다는 표시를 10초 TTL로 갱신
        redisTemplate.opsForValue().set(
                "play:heartbeat:" + request.getSessionId(),
                "alive",
                java.time.Duration.ofSeconds(10));
        return ResponseEntity.ok().build();
    }
}
