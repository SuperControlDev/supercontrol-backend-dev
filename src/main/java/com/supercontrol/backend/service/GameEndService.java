package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.redis.PlaySessionRedis;
import com.supercontrol.backend.domain.session.EndReason;
import com.supercontrol.backend.domain.session.PlaySessionStatus;
import com.supercontrol.backend.dto.GameEndResponse;
import com.supercontrol.backend.queue.QueueKey;
import com.supercontrol.backend.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameEndService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 🎯 게임 종료의 단일 진입점(기존 유지)
     *
     * 이 메서드는 다음 모든 경우에서 호출될 수 있음:
     * - FE에서 /game/end 호출 (사용자 종료 or 타이머 종료)
     * - HeartbeatMonitor에서 TIMEOUT 감지 시 호출
     * - RedisExpireListener에서 TTL 만료 시 호출
     *
     * ✅ 기존 기능(세션 제거/heartbeat 제거/락 해제)은 그대로 유지하고
     * ✅ FE가 필요로 하는 종료 결과를 JSON으로 돌려주기 위해 Response를 추가한다.
     *
     * ⚠️ 기존 호출부(HeartbeatMonitor/RedisExpireListener)는 return 값을 안 받아도 컴파일 OK
     */
    @Transactional
    public GameEndResponse endSession(String sessionId, EndReason reason) {

        // =========================================
        // 0️⃣ 종료 멱등 키
        // =========================================
        String endKey = "play:end:" + sessionId;
        Boolean firstEnd = redisTemplate.opsForValue()
                .setIfAbsent(endKey, "1", Duration.ofMinutes(5));

        long endedAt = System.currentTimeMillis();

        // 이미 종료 처리된 경우 → 바로 응답
        if (Boolean.FALSE.equals(firstEnd)) {
            log.debug("Duplicate endSession ignored. sessionId={}, reason={}", sessionId, reason);

            // ✅ 이미 종료되었더라도
            // FE가 USER_END로 호출한 경우 UX 관점에서 SUCCESS로 응답
            String result = (reason == EndReason.USER_END) ? "SUCCESS" : "FAIL";

            return GameEndResponse.builder()
                    .sessionId(sessionId)
                    .machineId(null)
                    .result(result)
                    .endedAt(endedAt)
                    .build();
        }

        // =========================================
        // 1️⃣ Redis 세션 조회
        // =========================================
        String sessionKey = "play:session:" + sessionId;
        String raw = redisTemplate.opsForValue().get(sessionKey);

        if (raw == null) {
            log.warn("End requested for non-existent session. sessionId={}", sessionId);
            return GameEndResponse.builder()
                    .sessionId(sessionId)
                    .machineId(null)
                    .result("FAIL")
                    .endedAt(endedAt)
                    .build();
        }

        // =========================================
        // 2️⃣ Redis 세션 파싱
        // =========================================
        PlaySessionRedis session = JsonUtils.fromJson(raw, PlaySessionRedis.class);
        Long machineId = session.getMachineId();

        // =========================================
        // 3️⃣ 중복 종료 방지 (PLAYING 체크)
        // =========================================
        if (session.getStatus() != PlaySessionStatus.PLAYING) {
            log.debug("Session not PLAYING. sessionId={}, status={}",
                    sessionId, session.getStatus());

            return GameEndResponse.builder()
                    .sessionId(sessionId)
                    .machineId(machineId)
                    .result(mapResult(reason))
                    .endedAt(endedAt)
                    .build();
        }

        log.info("End game session. sessionId={}, reason={}, machineId={}",
                sessionId, reason, machineId);

        // =========================================
        // 4️⃣ Redis 상태 정리 (기존 유지)
        // =========================================

        // (4-1) 게임 세션 제거
        redisTemplate.delete(sessionKey);

        // (4-2) heartbeat 제거
        redisTemplate.delete("play:heartbeat:" + sessionId);

        // (4-3) 머신 락 해제
        redisTemplate.delete("machine:lock:" + machineId);

        // =========================================
        // 5️⃣ 다음 대기자 깨우기 (✅ 추가)
        // =========================================
        processNext(machineId);

        log.info("Game session cleanup completed. sessionId={}", sessionId);

        // =========================================
        // 6️⃣ FE 응답
        // =========================================
        return GameEndResponse.builder()
                .sessionId(sessionId)
                .machineId(machineId)
                .result(mapResult(reason))
                .endedAt(endedAt)
                .build();
    }

    /**
     * EndReason를 SUCCESS/FAIL로 매핑
     *
     * FE가 요청한 포맷이 SUCCESS/FAIL 두 가지라서 단순화.
     * - USER_END(사용자 종료) => SUCCESS
     * - TIMEOUT/DISCONNECTED/SERVER_FORCE => FAIL
     */
    private String mapResult(EndReason reason) {
        if (reason == null)
            return "FAIL";
        return (reason == EndReason.USER_END) ? "SUCCESS" : "FAIL";
    }

    /**
     * Redis에는 machineId가 String으로 들어있어서 Long으로 파싱.
     * 파싱 실패 시 null 리턴(에러 방지)
     */
    private Long parseMachineId(String machineIdStr) {
        try {
            if (machineIdStr == null)
                return null;
            return Long.parseLong(machineIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 다음 대기자를 READY 상태로 전환
     *
     * ⚠️ 주의
     * - 큐 pop ❌
     * - startToken 발급 ❌
     * - reserved_check 로직이 알아서 처리하도록 "ready 힌트"만 줌
     */
    private void processNext(Long machineId) {

        String zsetKey = QueueKey.queueZset(machineId);

        // ZSET 맨 앞 entryId 조회
        Set<String> entries = redisTemplate.opsForZSet().range(zsetKey, 0, 0);
        if (entries == null || entries.isEmpty()) {
            log.debug("No waiting user for machineId={}", machineId);
            return;
        }

        String nextEntryId = entries.iterator().next();

        // ready 상태 힌트만 세팅
        redisTemplate.opsForValue()
                .set("queue:ready:" + machineId, nextEntryId, Duration.ofSeconds(30));

        log.info("Next queue entry awakened. machineId={}, entryId={}",
                machineId, nextEntryId);
    }

}
