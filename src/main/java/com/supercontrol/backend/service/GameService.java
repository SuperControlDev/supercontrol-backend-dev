package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.Machine;
import com.supercontrol.backend.domain.PlaySession;
import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.domain.redis.PlaySessionRedis;
import com.supercontrol.backend.domain.session.PlaySessionStatus;
import com.supercontrol.backend.dto.GameStartRequest;
import com.supercontrol.backend.dto.GameStartResponse;
import com.supercontrol.backend.redis.RedisLuaExecutor;
import com.supercontrol.backend.repository.MachineRepository;
import com.supercontrol.backend.repository.PlaySessionRepository;
import com.supercontrol.backend.repository.UserRepository;
import com.supercontrol.backend.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

        private final MachineRepository machineRepository;
        private final UserRepository userRepository;
        private final PlaySessionRepository playSessionRepository;
        private final RedisLuaExecutor redisLuaExecutor;

        // ✅ Redis 세션 저장용
        private final StringRedisTemplate redisTemplate;

        /**
         * 게임 시작
         *
         * ✅ 핵심 정책
         * - startToken 없이는 start 불가
         * - startToken은 1회용 + TTL
         * - Redis(Lua)에서 ready 상태 + 토큰 바인딩 검증 통과해야만 DB 로직 실행
         *
         * ✅ Lua 반환 규약
         * - 성공: queueEntryId (문자열)
         * - 실패: "ERR_..." prefix 또는 null
         */
        @Transactional
        public GameStartResponse startGame(GameStartRequest request) {

                /*
                 * =====================================================
                 * 0️⃣ Redis Gate (Lua) - 기존 유지
                 * =====================================================
                 */
                String luaResult = redisLuaExecutor.executeGameStart(
                                request.getStartToken(),
                                request.getUserId(),
                                request.getMachineId());

                if (luaResult == null || luaResult.startsWith("ERR_")) {
                        return GameStartResponse.builder()
                                        .success(false)
                                        .reason("INVALID_START_TOKEN_OR_NOT_READY")
                                        .build();
                }

                String queueEntryId = luaResult;
                System.out.println("[SuperCon Log] start approved. queueEntryId = " + queueEntryId);

                /*
                 * =====================================================
                 * 1️⃣ 사용자 조회 (기존 유지)
                 * =====================================================
                 */
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

                /*
                 * =====================================================
                 * 2️⃣ 기계 조회 (기존 유지)
                 * =====================================================
                 */
                Machine machine = machineRepository.findById(request.getMachineId())
                                .orElseThrow(() -> new RuntimeException("MACHINE_NOT_FOUND"));

                /*
                 * =====================================================
                 * 3️⃣ free_ticket 확인/차감 (요청사항 반영)
                 * =====================================================
                 */
                if (user.getFreeTickets() <= 0) {
                        return GameStartResponse.builder()
                                        .success(false)
                                        .reason("NOT_ENOUGH_FREE_TICKET")
                                        .remainingFreeTickets(user.getFreeTickets())
                                        .build();
                }

                user.setFreeTickets(user.getFreeTickets() - 1);
                userRepository.save(user);

                /*
                 * =====================================================
                 * 4️⃣ ✅ Redis 세션 ID 생성 (핵심)
                 * - 외부(FE)에서 end 호출 시 사용할 값
                 * =====================================================
                 */
                String redisSessionId = UUID.randomUUID().toString();

                /*
                 * =====================================================
                 * 5️⃣ DB PlaySession 생성 (기존 유지 + redisSessionId만 추가)
                 * =====================================================
                 */
                int durationSec = 45;

                PlaySession session = PlaySession.builder()
                                .redisSessionId(redisSessionId)
                                .user(user)
                                .machine(machine)
                                .startTime(LocalDateTime.now())
                                .endTime(null)
                                .status("active")
                                .durationSec(durationSec)
                                .ended(false)
                                .endedAt(null)
                                .endReason(null)
                                .build();

                session = playSessionRepository.save(session);

                /*
                 * =====================================================
                 * 6️⃣ ✅ Redis play:session:{redisSessionId} 저장 (GameEndService와 정합)
                 * =====================================================
                 */
                long startAt = System.currentTimeMillis();
                long expireAt = startAt + (durationSec * 1000L);

                PlaySessionRedis redisSession = new PlaySessionRedis(
                                redisSessionId,
                                user.getUserId(),
                                machine.getMachineId(),
                                startAt,
                                expireAt,
                                PlaySessionStatus.PLAYING);

                // TTL은 durationSec + 여유(예: 30초)
                Duration ttl = Duration.ofSeconds(durationSec + 30L);

                redisTemplate.opsForValue().set(
                                "play:session:" + redisSessionId,
                                JsonUtils.toJson(redisSession),
                                ttl);

                /*
                 * =====================================================
                 * 7️⃣ Response 생성
                 * - sessionId: ✅ Redis 세션 ID(String)
                 * - dbSessionId: 기존 기능 호환용
                 * =====================================================
                 */
                long startTimeMillis = session.getStartTime()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();

                return GameStartResponse.builder()
                                .success(true)
                                // legacy 필드 유지(사용 안 하면 0으로 둬도 됨)
                                .remainingCoins(user.getBalance())
                                .remainingFreeTickets(user.getFreeTickets())
                                .reason(null)
                                .gameStartTime(startTimeMillis)
                                .durationSec(durationSec)
                                .sessionId(redisSessionId) // ✅ FE/End는 이 값 사용
                                .dbSessionId(session.getSessionId()) // ✅ 필요 시만 사용
                                .build();
        }
}
