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
     * 3초마다 PLAYING 세션들의 heartbeat 존재 여부를 확인한다.
     * - heartbeat 키가 없으면 → DISCONNECTED로 endSession 처리
     */
    @Scheduled(fixedDelay = 3000)
    public void checkHeartbeat() {
        Set<String> sessionKeys = redisTemplate.keys("play:session:*");
        if (sessionKeys == null || sessionKeys.isEmpty())
            return;

        for (String sessionKey : sessionKeys) {
            String sessionId = sessionKey.replace("play:session:", "");
            Boolean alive = redisTemplate.hasKey("play:heartbeat:" + sessionId);

            if (Boolean.FALSE.equals(alive)) {
                log.info("Heartbeat missing. Force end. sessionId={}", sessionId);
                gameEndService.endSession(sessionId, EndReason.DISCONNECTED);
            }
        }
    }

    /**
     * Redis TTL 만료 이벤트 누락 대비용 TIMEOUT fallback
     * - TTL 이벤트가 안 들어와도 게임이 끝나도록 보장
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

            if (session.getExpireAt() <= now) {
                log.info("TIMEOUT fallback triggered. sessionId={}", sessionId);
                gameEndService.endSession(sessionId, EndReason.TIMEOUT);
            }
        }
    }

}
