package com.supercontrol.backend.service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.supercontrol.backend.dto.QueueStatusResponse;
import com.supercontrol.backend.queue.QueueKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;

    // ✅ 운영 시 권장: 30~60초
    // 현재는 디버깅 편의를 위해 3000초로 설정 (원하면 30으로)
    private static final long START_TOKEN_TTL_SEC = 3000;

    // ✅ ready 점유 방지용(선택) - 토큰 TTL과 동일하게 맞추면 단순
    private static final long READY_TTL_SEC = START_TOKEN_TTL_SEC;

    /**
     * 대기열 진입
     * - 이미 대기 중이면 중복 진입 방지
     */
    public String enterQueue(String userId, Long machineId) {

        String zsetKey = QueueKey.queueZset(machineId);

        // 중복 입장 방지 (userId 기준)
        Set<String> entries = redisTemplate.opsForZSet().range(zsetKey, 0, -1);
        if (entries != null) {
            for (String entryId : entries) {
                Object uidObj = redisTemplate.opsForHash()
                        .get(QueueKey.queueEntry(entryId), "userId");

                if (uidObj != null && uidObj.toString().equals(userId)) {
                    return entryId; // 이미 대기 중
                }
            }
        }

        // 새 QueueEntry 생성
        String queueEntryId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        // ZSET에 삽입 (score = joinedAt)
        redisTemplate.opsForZSet().add(zsetKey, queueEntryId, now);

        // 메타 정보 저장
        String entryKey = QueueKey.queueEntry(queueEntryId);
        redisTemplate.opsForHash().put(entryKey, "userId", userId);
        redisTemplate.opsForHash().put(entryKey, "machineId", machineId.toString());
        redisTemplate.opsForHash().put(entryKey, "state", "waiting");
        redisTemplate.opsForHash().put(entryKey, "joinedAt", String.valueOf(now));

        return queueEntryId;
    }

    /**
     * reserved_check (폴링용)
     *
     * ✅ 중요한 원칙
     * - ready 상태에서는 startToken을 "재발급"하지 않고 "재사용"해야 함
     * - 그렇지 않으면 polling할 때마다 토큰이 바뀌고 /game/start가 계속 실패할 수 있음
     */
    public QueueStatusResponse checkStatus(String userId, Long machineId) {

        String zsetKey = QueueKey.queueZset(machineId);

        // 1) 큐에 있는지 확인 + 내 entryId 찾기 + 내 position 계산
        Set<String> entries = redisTemplate.opsForZSet().range(zsetKey, 0, -1);
        if (entries == null || entries.isEmpty()) {
            return new QueueStatusResponse(null, "waiting", false, null, null);
        }

        String myEntryId = null;
        int index = 0;

        for (String entryId : entries) {
            Object uid = redisTemplate.opsForHash().get(QueueKey.queueEntry(entryId), "userId");
            if (uid != null && uid.toString().equals(userId)) {
                myEntryId = entryId;
                break;
            }
            index++;
        }

        if (myEntryId == null) {
            // 큐 미진입 상태
            return new QueueStatusResponse(null, "waiting", false, null, null);
        }

        int position = index + 1;

        // 2) 내가 head 인가?
        String headEntryId = entries.iterator().next();
        if (!headEntryId.equals(myEntryId)) {
            return new QueueStatusResponse(position, "waiting", false, null, null);
        }

        // 3) head라면 ready 상태 처리
        String readyKey = "queue:ready:" + machineId;
        String readyTokenKey = "queue:readyToken:" + machineId;

        String currentReadyEntryId = redisTemplate.opsForValue().get(readyKey);

        // ✅ (A) 이미 ready 상태가 "나"로 잡혀있으면 → 기존 토큰 재사용
        if (currentReadyEntryId != null && currentReadyEntryId.equals(myEntryId)) {

            String existingToken = redisTemplate.opsForValue().get(readyTokenKey);

            if (existingToken != null) {
                String existingTokenKey = "startToken:" + existingToken;

                Boolean tokenExists = redisTemplate.hasKey(existingTokenKey);
                if (Boolean.TRUE.equals(tokenExists)) {
                    // TTL 기반으로 expiresAt 계산 (FE 카운트다운용)
                    Long ttl = redisTemplate.getExpire(existingTokenKey);
                    long expiresAt = System.currentTimeMillis()
                            + ((ttl != null && ttl > 0) ? ttl * 1000 : START_TOKEN_TTL_SEC * 1000);

                    return new QueueStatusResponse(
                            position,
                            "ready",
                            true,
                            existingToken,
                            expiresAt);
                }
            }

            // 여기까지 왔다면:
            // - readyTokenKey는 있는데 startToken이 만료/삭제됨
            // - 또는 readyTokenKey가 비어있음
            // → 새 토큰 발급으로 복구
        }

        // ✅ (B) ready가 비어있거나/다른 값이거나/토큰이 만료된 경우 → 새로 세팅(단, 이전 토큰 정리)
        // 이전 토큰이 있으면 정리(토큰 누적 방지)
        String oldToken = redisTemplate.opsForValue().get(readyTokenKey);
        if (oldToken != null) {
            redisTemplate.delete("startToken:" + oldToken);
            redisTemplate.delete(readyTokenKey);
        }

        // readyKey를 내 entryId로 세팅 + TTL (점유 방지)
        redisTemplate.opsForValue().set(readyKey, myEntryId, Duration.ofSeconds(READY_TTL_SEC));

        // startToken 발급
        String token = UUID.randomUUID().toString();
        String tokenKey = "startToken:" + token;

        // startToken 바인딩 저장 + TTL
        // 값 포맷: userId:machineId:queueEntryId (Lua가 split해서 검증)
        redisTemplate.opsForValue().set(
                tokenKey,
                userId + ":" + machineId + ":" + myEntryId,
                Duration.ofSeconds(START_TOKEN_TTL_SEC));

        // readyTokenKey에 "현재 토큰" 저장 (reserved_check 멱등 보장)
        redisTemplate.opsForValue().set(
                readyTokenKey,
                token,
                Duration.ofSeconds(START_TOKEN_TTL_SEC));

        long expiresAt = System.currentTimeMillis() + START_TOKEN_TTL_SEC * 1000;

        return new QueueStatusResponse(
                position,
                "ready",
                true,
                token,
                expiresAt);
    }
}
