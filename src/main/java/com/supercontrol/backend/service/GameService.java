package com.supercontrol.backend.service;

import com.supercontrol.backend.domain.Machine;
import com.supercontrol.backend.domain.User;
import com.supercontrol.backend.domain.PlaySession;
import com.supercontrol.backend.dto.GameStartRequest;
import com.supercontrol.backend.dto.GameStartResponse;
import com.supercontrol.backend.repository.MachineRepository;
import com.supercontrol.backend.repository.UserRepository;
import com.supercontrol.backend.repository.PlaySessionRepository;

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

        @Transactional
        public GameStartResponse startGame(GameStartRequest request) {

                // 1) 사용자 조회
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

                // 2) 기계 조회
                Machine machine = machineRepository.findById(request.getMachineId())
                                .orElseThrow(() -> new RuntimeException("MACHINE_NOT_FOUND"));

                System.out.println("유저 잔액 : " + user.getBalance());
                System.out.println("기계 사용료 : " + machine.getPrice());

                // 3) 잔액 확인
                if (user.getBalance() < machine.getPrice()) {
                        return GameStartResponse.builder()
                                        .success(false)
                                        .reason("NOT_ENOUGH_COINS")
                                        .remainingCoins(user.getBalance())
                                        .build();
                }

                // 4) 코인 차감
                user.setBalance(user.getBalance() - machine.getPrice());
                userRepository.save(user);

                // 5) 플레이 세션 생성 (durationSec = 45초 고정)
                PlaySession session = PlaySession.builder()
                                .user(user)
                                .machine(machine)
                                .startTime(LocalDateTime.now())
                                .endTime(null)
                                .status("active")
                                .durationSec(45)
                                .build();

                playSessionRepository.save(session);

                // 6) Response 생성
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
