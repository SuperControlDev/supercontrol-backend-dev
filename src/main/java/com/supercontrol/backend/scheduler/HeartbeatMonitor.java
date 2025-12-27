package com.supercontrol.backend.scheduler;

import com.supercontrol.backend.domain.redis.PlaySessionRedis;
import com.supercontrol.backend.domain.session.EndReason;
import com.supercontrol.backend.service.GameEndService;
import com.supercontrol.backend.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatMonitor {

    private final StringRedisTemplate redisTemplate;
    private final GameEndService gameEndService;

    /**
     * [1] Heartbeat 체크
     * - 3초마다 PLAYING 세션 중 heartbeat 키가 없는 세션을 강제 종료
     * - 네트워크 단절 / 브라우저 종료 대응
     */
    @Scheduled(fixedDelay = 3000)
    public void checkHeartbeat() {
        Set<String> sessionKeys = redisTemplate.keys("play:session:*");
        if (sessionKeys == null || sessionKeys.isEmpty())
            return;

        for (String sessionKey : sessionKeys) {
            // play:session:{sessionId} → sessionId
            String sessionId = sessionKey.replace("play:session:", "");

            Boolean alive = redisTemplate.hasKey("play:heartbeat:" + sessionId);

            if (Boolean.FALSE.equals(alive)) {
                log.info("Heartbeat missing. Force end. sessionId={}", sessionId);
                gameEndService.endSession(sessionId, EndReason.DISCONNECTED);
            }
        }
    }

    /**
     * [2] TTL 만료 이벤트 누락 대비 fallback
     * - Redis keyspace notification이 안 와도 게임 종료를 보장
     */
    @Scheduled(fixedDelay = 3000)
    public void timeoutFallbackCheck() {
        Set<String> sessionKeys = redisTemplate.keys("play:session:*");
        if (sessionKeys == null || sessionKeys.isEmpty())
            return;

        long now = System.currentTimeMillis();

        for (String sessionKey : sessionKeys) {
            String sessionId = sessionKey.replace("play:session:", "");

            String raw = redisTemplate.opsForValue().get(sessionKey);
            if (raw == null)
                continue;

            PlaySessionRedis session = JsonUtils.fromJson(raw, PlaySessionRedis.class);

            // expireAt 기준 TIMEOUT 판단
            if (session.getExpireAt() <= now) {
                log.info("TIMEOUT fallback triggered. sessionId={}", sessionId);
                gameEndService.endSession(sessionId, EndReason.TIMEOUT);
            }
        }
    }
}
