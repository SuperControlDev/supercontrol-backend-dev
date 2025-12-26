package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.redis.PlaySessionRedis;
import com.supercontrol.backend.domain.session.EndReason;
import com.supercontrol.backend.domain.session.PlaySessionStatus;
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

    /*
     * ==============================
     * [나중에 적용할 의존성]
     * ==============================
     *
     * 1️⃣ DB 세션 종료용 (운영 안정화 이후)
     *
     * private final PlaySessionRepository playSessionRepository;
     *
     * 2️⃣ 대기열 처리 (Queue 연동 이후)
     *
     * private final QueueService queueService;
     *
     */

    /**
     * 🎯 게임 종료의 단일 진입점
     *
     * 이 메서드는 다음 모든 경우에서 호출된다:
     * - 사용자가 /api/game/end 호출
     * - Redis TTL 만료 (TIMEOUT)
     * - heartbeat 끊김 (DISCONNECTED)
     *
     * ❗ 주의:
     * - 절대 다른 곳에서 종료 로직을 직접 구현하지 말 것
     * - 이 메서드는 idempotent 해야 함 (중복 호출 안전)
     */
    @Transactional
    public void endSession(String sessionId, EndReason reason) {

        String sessionKey = "play:session:" + sessionId;

        // ==============================
        // 0️⃣ Redis 세션 존재 여부 확인
        // ==============================
        String raw = redisTemplate.opsForValue().get(sessionKey);

        if (raw == null) {
            // 이미 종료되었거나 TTL로 정리된 상태
            log.debug("Session already ended. sessionId={}", sessionId);
            return;
        }

        PlaySessionRedis session = JsonUtils.fromJson(raw, PlaySessionRedis.class);

        if (session.getStatus() != PlaySessionStatus.PLAYING) {
            // 중복 종료 방지
            log.debug("Session not PLAYING. sessionId={}, status={}",
                    sessionId, session.getStatus());
            return;
        }

        log.info("End game session. sessionId={}, reason={}",
                sessionId, reason);

        // ==============================
        // 1️⃣ [나중에] DB 세션 종료 처리
        // ==============================
        /*
         * 🔜 운영 단계에서 반드시 필요
         *
         * - 게임 종료 이력 저장
         * - 통계 / 리플레이 / CS 대응
         *
         * 예시:
         *
         * PlaySession entity =
         * playSessionRepository.findBySessionId(sessionId)
         * .orElseThrow();
         *
         * entity.end(reason); // endAt, endReason 기록
         * playSessionRepository.save(entity);
         */

        // ==============================
        // 2️⃣ Redis 상태 정리 (🔥 지금 반드시 필요)
        // ==============================

        // 게임 세션 제거
        redisTemplate.delete(sessionKey);

        // heartbeat 제거
        redisTemplate.delete("play:heartbeat:" + sessionId);

        // 머신 락 해제 (다음 유저를 위해)
        redisTemplate.delete("machine:lock:" + session.getMachineId());

        // ==============================
        // 3️⃣ [나중에] 다음 대기자 처리
        // ==============================
        /*
         * 🔜 Queue 연동 단계에서 구현
         *
         * - 대기열 pop
         * - 다음 유저 startToken 발급
         * - FE에 "입장 가능" 이벤트 전달
         *
         * 예시:
         *
         * queueService.wakeNext(session.getMachineId());
         */

        // ==============================
        // 4️⃣ [선택] 종료 이벤트 발행
        // ==============================
        /*
         * 🔜 WebSocket / SSE 사용 시
         *
         * - FE에 즉시 종료 알림
         * - 관제 대시보드 업데이트
         */

        log.info("Game session cleanup completed. sessionId={}", sessionId);
    }
}
