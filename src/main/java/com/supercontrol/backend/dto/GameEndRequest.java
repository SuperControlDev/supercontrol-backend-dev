package com.supercontrol.backend.dto;

import com.supercontrol.backend.domain.session.EndReason;
import lombok.Data;

@Data
public class GameEndRequest {

    /** 종료할 세션 ID */
    private String sessionId;

    /** 종료 사유 */
    private EndReason reason;
}
