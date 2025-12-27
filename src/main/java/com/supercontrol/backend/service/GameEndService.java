package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.redis.PlaySessionRedis;
import com.supercontrol.backend.domain.session.EndReason;
import com.supercontrol.backend.domain.session.PlaySessionStatus;
import com.supercontrol.backend.dto.GameEndResponse;
import com.supercontrol.backend.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        // 0️⃣ Redis에 세션이 존재하는지 확인
        // =========================================
        // 기존에 사용하던 키 형식 그대로 유지
        String sessionKey = "play:session:" + sessionId;

        // Redis에는 PlaySessionRedis(JSON 문자열)가 들어있음
        String raw = redisTemplate.opsForValue().get(sessionKey);

        // endedAt은 응답을 위해 지금 시각을 항상 찍어준다
        long endedAt = System.currentTimeMillis();

        // =========================================
        // 0-1) 이미 종료되었거나 TTL로 정리된 상태
        // =========================================
        if (raw == null) {
            log.warn("End requested for non-existent session. sessionId={}", sessionId);

            return GameEndResponse.builder()
                    .sessionId(sessionId)
                    .machineId(null)
                    .result("FAIL") // ⭐ 핵심 변경
                    .endedAt(endedAt)
                    .build();
        }

        // =========================================
        // 0-2) Redis 세션 파싱
        // =========================================
        PlaySessionRedis session = JsonUtils.fromJson(raw, PlaySessionRedis.class);

        // machineId는 Redis 세션에 문자열로 저장되어 있음
        // Long machineId = parseMachineId(session.getMachineId());
        Long machineId = session.getMachineId();

        // =========================================
        // 0-3) 중복 종료 방지 (기존 로직 유지)
        // =========================================
        // PLAYING 상태가 아니라면 이미 종료되었거나 종료 처리 중일 수 있음
        if (session.getStatus() != PlaySessionStatus.PLAYING) {
            log.debug("Session not PLAYING. sessionId={}, status={}",
                    sessionId, session.getStatus());

            // 기존은 return void였는데, 이제 FE를 위해 응답만 내려줌
            return GameEndResponse.builder()
                    .sessionId(sessionId)
                    .machineId(machineId)
                    .result(mapResult(reason))
                    .endedAt(endedAt)
                    .build();
        }

        log.info("End game session. sessionId={}, reason={}, machineId={}",
                sessionId, reason, session.getMachineId());

        // =========================================
        // 1️⃣ [나중에] DB 세션 종료 처리 (기존 주석 유지)
        // =========================================
        /*
         * 🔜 운영 단계에서 반드시 필요
         *
         * - PlaySession(Entity) 조회 후 endTime/상태 업데이트
         * - durationSec 계산
         * - 사용자 통계/결제/정산 등 반영
         *
         * 현재는 개발 단계이므로 Redis 기준으로만 종료 처리한다.
         */

        // =========================================
        // 2️⃣ Redis 상태 정리 (🔥 지금 반드시 필요) - 기존 유지
        // =========================================

        // (2-1) 게임 세션 제거
        redisTemplate.delete(sessionKey);

        // (2-2) heartbeat 제거
        redisTemplate.delete("play:heartbeat:" + sessionId);

        // (2-3) 머신 락 해제 (다음 유저를 위해) - 기존 유지
        // 기존 코드가 "machine:lock:<machineId>" 형태를 쓰고 있음
        redisTemplate.delete("machine:lock:" + session.getMachineId());

        // =========================================
        // 3️⃣ [나중에] 다음 대기자 처리 (기존 주석 유지)
        // =========================================
        /*
         * 🔜 Queue 연동 단계에서 구현
         *
         * - 대기열 pop
         * - 다음 유저 startToken 발급
         * - FE에 "입장 가능" 이벤트 전달
         *
         * 예시:
         * queueService.wakeNext(session.getMachineId());
         */

        // =========================================
        // 4️⃣ [선택] 종료 이벤트 발행 (기존 주석 유지)
        // =========================================
        /*
         * 🔜 WebSocket / SSE 사용 시
         * - FE에 즉시 종료 알림
         * - 관제 대시보드 업데이트
         */

        log.info("Game session cleanup completed. sessionId={}", sessionId);

        // =========================================
        // ✅ FE 요구사항: 종료 결과를 JSON으로 리턴
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
}
