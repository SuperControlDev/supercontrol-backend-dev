package com.supercontrol.backend.domain.redis;

import com.supercontrol.backend.domain.session.PlaySessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaySessionRedis {

    /** 게임 세션 ID */
    private String sessionId;

    /** 사용자 ID */
    private String userId;

    /** 머신 ID (인형뽑기 기계) */
    private Long machineId;

    /** 게임 시작 시각 (epoch millis) */
    private long startAt;

    /** 게임 종료 예정 시각 */
    private long expireAt;

    /** 현재 상태 */
    private PlaySessionStatus status;
}
