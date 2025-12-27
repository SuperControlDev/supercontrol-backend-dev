package com.supercontrol.backend.controller;

import com.supercontrol.backend.domain.redis.PlaySessionRedis;
import com.supercontrol.backend.domain.session.PlaySessionStatus;
import com.supercontrol.backend.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final StringRedisTemplate redisTemplate;

    /**
     * 테스트용 가짜 게임 세션 생성
     */
    @PostMapping("/create-session")
    public String createSession(
            @RequestParam String sessionId,
            @RequestParam Long machineId,
            @RequestParam(defaultValue = "30") long ttlSeconds) {
        long now = System.currentTimeMillis();

        PlaySessionRedis session = new PlaySessionRedis(
                sessionId,
                "test-user",
                machineId,
                now,
                now + (ttlSeconds * 1000),
                PlaySessionStatus.PLAYING);

        redisTemplate.opsForValue().set(
                "play:session:" + sessionId,
                JsonUtils.toJson(session),
                Duration.ofSeconds(ttlSeconds));

        redisTemplate.opsForValue().set("machine:lock:" + machineId, "locked");
        redisTemplate.opsForValue().set(
                "play:heartbeat:" + sessionId,
                "alive",
                Duration.ofSeconds(10));

        return "OK";
    }
}
