package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.Machine;
import com.supercontrol.backend.domain.PlaySession;
import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.dto.GameStartRequest;
import com.supercontrol.backend.dto.GameStartResponse;
import com.supercontrol.backend.redis.RedisLuaExecutor;
import com.supercontrol.backend.repository.MachineRepository;
import com.supercontrol.backend.repository.PlaySessionRepository;
import com.supercontrol.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class GameService {

        private final MachineRepository machineRepository;
        private final UserRepository userRepository;
        private final PlaySessionRepository playSessionRepository;
        private final RedisLuaExecutor redisLuaExecutor;

        /**
         * 게임 시작
         *
         * ✅ 핵심 정책
         * - startToken 없이는 start 불가
         * - startToken은 1회용 + TTL
         * - Redis(Lua)에서 ready 상태 + 토큰 바인딩 검증 통과해야만
         * DB 로직(코인 차감 / 세션 생성)이 실행됨
         *
         * ✅ Lua 반환 규약(매우 중요)
         * - 성공: queueEntryId (문자열)
         * - 실패: "ERR_..." 로 시작하는 문자열 (또는 null)
         *
         * 팁
         * - Lua 성공 값을 "OK"로 비교하면 절대 안 됩니다.
         * 성공 값은 queueEntryId 자체입니다.
         */
        @Transactional
        public GameStartResponse startGame(GameStartRequest request) {

                /*
                 * =====================================================
                 * 0️⃣ Redis Gate (보안/정합성 관문) - 수정 핵심
                 * =====================================================
                 */
                String luaResult = redisLuaExecutor.executeGameStart(
                                request.getStartToken(),
                                request.getUserId(),
                                request.getMachineId());

                // ✅ 실패 판단 규칙: null 또는 "ERR_" prefix
                if (luaResult == null || luaResult.startsWith("ERR_")) {
                        return GameStartResponse.builder()
                                        .success(false)
                                        .reason("INVALID_START_TOKEN_OR_NOT_READY")
                                        .build();
                }

                // ✅ 성공 시 luaResult 자체가 queueEntryId
                String queueEntryId = luaResult; // (현재는 사용처가 없지만, 추후 이력/로그/추적에 활용 가능)

                // 팁: 문제 추적용으로 남겨두면 운영 때 매우 유용합니다.
                System.out.println("[SuperCon Log] start approved. queueEntryId = " + queueEntryId);

                /*
                 * =====================================================
                 * 1️⃣ 사용자 조회 (기존 기능 유지)
                 * =====================================================
                 */
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

                /*
                 * =====================================================
                 * 2️⃣ 기계 조회 (기존 기능 유지)
                 * =====================================================
                 */
                Machine machine = machineRepository.findById(request.getMachineId())
                                .orElseThrow(() -> new RuntimeException("MACHINE_NOT_FOUND"));

                System.out.println("유저 잔액 : " + user.getBalance());
                System.out.println("기계 사용료 : " + machine.getPrice());

                /*
                 * =====================================================
                 * 3️⃣ 잔액 확인 (기존 기능 유지)
                 * =====================================================
                 */
                if (user.getBalance() < machine.getPrice()) {
                        return GameStartResponse.builder()
                                        .success(false)
                                        .reason("NOT_ENOUGH_COINS")
                                        .remainingCoins(user.getBalance())
                                        .build();
                }

                /*
                 * =====================================================
                 * 4️⃣ 코인 차감 (기존 기능 유지)
                 * =====================================================
                 */
                user.setBalance(user.getBalance() - machine.getPrice());
                userRepository.save(user);

                /*
                 * =====================================================
                 * 5️⃣ 플레이 세션 생성 (기존 기능 유지)
                 * - durationSec = 45초 고정
                 * =====================================================
                 */
                PlaySession session = PlaySession.builder()
                                .user(user)
                                .machine(machine)
                                .startTime(LocalDateTime.now())
                                .endTime(null)
                                .status("active")
                                .durationSec(45)
                                .build();

                playSessionRepository.save(session);

                /*
                 * =====================================================
                 * 6️⃣ Response 생성 (기존 기능 유지)
                 * =====================================================
                 */
                long startTimeMillis = session.getStartTime()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();

                return GameStartResponse.builder()
                                .success(true)
                                .remainingCoins(user.getBalance())
                                .gameStartTime(startTimeMillis)
                                .durationSec(session.getDurationSec())
                                .sessionId(session.getSessionId())
                                .build();
        }
}
