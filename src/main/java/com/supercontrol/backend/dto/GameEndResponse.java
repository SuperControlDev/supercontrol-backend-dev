package com.supercontrol.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GameEndResponse {

    private String sessionId;
    private Long machineId;
    private String result; // SUCCESS | FAIL
    private Long endedAt; // epoch millis
}
