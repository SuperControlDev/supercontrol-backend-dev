package com.supercontrol.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_l_playsession")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    /**
     * ✅ Redis 세션 ID (외부 공개/실시간 제어용)
     * - GameEndService는 play:session:{redisSessionId} 형태로 종료 처리하므로 반드시 필요
     */
    @Column(name = "redis_session_id", nullable = false, unique = true, length = 64)
    private String redisSessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "ended")
    private boolean ended;

    @Column(name = "ended_at")
    private Long endedAt;

    @Column(name = "end_reason")
    private String endReason; // USER_EXIT, TIMEOUT 등
}
