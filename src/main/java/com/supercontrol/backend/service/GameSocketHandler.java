package com.supercontrol.backend.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.AckRequest;
import com.supercontrol.backend.dto.SessionDto;
import com.supercontrol.backend.dto.GrabResultDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameSocketHandler {

    private final SocketIOServer server;

    // 세션 저장 (MVP에선 메모리)
    private final Map<String, SessionDto> sessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 클라이언트 연결시
        server.addConnectListener(client -> {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            System.out.println("Socket connected: " + client.getSessionId() + " userId=" + userId);
        });

        server.addDisconnectListener(client -> {
            System.out.println("Socket disconnected: " + client.getSessionId());
        });

        // 세션 생성
        server.addEventListener("session:create", SessionCreateRequest.class,
                (client, data, ackSender) -> handleSessionCreate(client, data));

        // 세션 참가
        server.addEventListener("session:join", SessionJoinRequest.class,
                (client, data, ackSender) -> handleSessionJoin(client, data));

        // 세션 나가기
        server.addEventListener("session:leave", SessionLeaveRequest.class,
                (client, data, ackSender) -> handleSessionLeave(client, data));

        // 클로 이동
        server.addEventListener("game:move", GameMoveRequest.class,
                (client, data, ackSender) -> handleGameMove(client, data));

        // 드롭
        server.addEventListener("game:drop", VoidRequest.class,
                (client, data, ackSender) -> handleGameDrop(client));

        // 그랩
        server.addEventListener("game:grab", VoidRequest.class,
                (client, data, ackSender) -> handleGameGrab(client));
    }

    private void handleSessionCreate(SocketIOClient client, SessionCreateRequest req) {
        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        int duration = 15;

        SessionDto session = SessionDto.builder()
                .sessionId(sessionId)
                .machineId(req.getMachineId())
                .userId(req.getUserId())
                .startAt(now)
                .expiresAt(now + duration * 1000L)
                .durationSec(duration)
                .status("active")
                .build();

        sessions.put(sessionId, session);

        client.joinRoom(session.getSessionId());

        client.sendEvent("session:created", Map.of("session", session));
    }

    private void handleSessionJoin(SocketIOClient client, SessionJoinRequest req) {
        SessionDto session = sessions.get(req.getSessionId());
        if (session == null) {
            client.sendEvent("session:failed",
                    Map.of("session", (Object) null, "reason", "session_not_found"));
            return;
        }
        client.joinRoom(session.getSessionId());
        client.sendEvent("session:joined", Map.of("session", session));
    }

    private void handleSessionLeave(SocketIOClient client, SessionLeaveRequest req) {
        client.leaveRoom(req.getSessionId());
        SessionDto session = sessions.get(req.getSessionId());
        if (session != null) {
            session.setStatus("ended");
            client.sendEvent("session:ended", Map.of("session", session));
        }
    }

    private void handleGameMove(SocketIOClient client, GameMoveRequest req) {
        // 실제로는 하드웨어 제어 + 현재 위치 계산
        // var gameState = Map.of(
        // "position", Map.of("x", 0, "y", 0, "z", 0),
        // "clawState", "moving");
        Map<String, Object> gameState = Map.of(
                "position", Map.of("x", 0, "y", 0, "z", 0),
                "clawState", "moving");
        client.sendEvent("game:state", Map.of("gameState", gameState));
    }

    private void handleGameDrop(SocketIOClient client) {
        // var gameState = Map.of(
        // "position", Map.of("x", 0, "y", 0, "z", -1),
        // "clawState", "dropping"
        // );
        Map<String, Object> gameState = Map.of(
                "position", Map.of("x", 0, "y", 0, "z", 0),
                "clawState", "moving");

        client.sendEvent("game:state", Map.of("gameState", gameState));
    }

    private void handleGameGrab(SocketIOClient client) {
        GrabResultDto result = GrabResultDto.builder()
                .success(true)
                .reason(null)
                .timestamp(System.currentTimeMillis())
                .build();

        client.sendEvent("game:result", Map.of("result", result));
    }

    // ====== 요청 DTO ======

    public static class SessionCreateRequest {
        private String userId;
        private int machineId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getMachineId() {
            return machineId;
        }

        public void setMachineId(int machineId) {
            this.machineId = machineId;
        }
    }

    public static class SessionJoinRequest {
        private String sessionId;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    public static class SessionLeaveRequest {
        private String sessionId;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    public static class GameMoveRequest {
        private String direction;

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }

    public static class VoidRequest {
    }
}
