package com.supercontrol.backend.redis;

import com.supercontrol.backend.domain.session.EndReason;
import com.supercontrol.backend.service.GameEndService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisExpireListener {

    private final GameEndService gameEndService;

    @EventListener
    public void onExpired(RedisKeyExpiredEvent<byte[]> event) {
        // 만료된 key는 byte[]로 들어옴 (Spring 버전별로 getKey()가 없을 수 있음)
        String expiredKey = new String(event.getSource(), StandardCharsets.UTF_8);

        log.info("Redis key expired: {}", expiredKey);

        if (expiredKey.startsWith("play:session:")) {
            String sessionId = expiredKey.replace("play:session:", "");
            gameEndService.endSession(sessionId, EndReason.TIMEOUT);
        }
    }
}
