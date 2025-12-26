package com.supercontrol.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supercontrol.backend.dto.QueueStatusResponse;
import com.supercontrol.backend.repository.MachineRepository;
import com.supercontrol.backend.repository.UserRepository;
import com.supercontrol.backend.service.QueueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final UserRepository userRepository;
    private final MachineRepository machineRepository;

    @PostMapping("/enter")
    public ResponseEntity<?> enter(
            @RequestParam String userId,
            @RequestParam Long machineId) {
        // 1️⃣ 유저 존재 검증
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest()
                    .body("존재하지 않는 userId 입니다.");
        }

        // 2️⃣ 머신 존재 검증
        if (!machineRepository.existsById(machineId)) {
            return ResponseEntity.badRequest()
                    .body("존재하지 않는 machineId 입니다.");
        }

        // 3️⃣ Redis Queue 진입
        String queueEntryId = queueService.enterQueue(userId, machineId);

        return ResponseEntity.ok(
                Map.of(
                        "queueEntryId", queueEntryId,
                        "message", "QUEUE_ENTERED"));
    }

    @GetMapping("/reserved_check")
    public ResponseEntity<QueueStatusResponse> check(
            @RequestParam String userId,
            @RequestParam Long machineId) {
        QueueStatusResponse response = queueService.checkStatus(userId, machineId);

        return ResponseEntity.ok(response);
    }

}
