package com.supercontrol.backend.redis;

import com.supercontrol.backend.domain.session.EndReason;
import com.supercontrol.backend.service.GameEndService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisExpireListener {

    private final GameEndService gameEndService;

    @EventListener
    public void onMessage(RedisKeyExpiredEvent event) {

        // ⚠️ 만료된 key는 getSource()로 가져온다
        String expiredKey = event.getSource().toString();
        // 예: play:session:test-session-1

        log.info("Redis key expired: {}", expiredKey);

        if (!expiredKey.startsWith("play:session:")) {
            return;
        }

        // ✅ 순수 sessionId 추출
        String sessionId = expiredKey.replace("play:session:", "");

        gameEndService.endSession(sessionId, EndReason.TIMEOUT);
    }
}
