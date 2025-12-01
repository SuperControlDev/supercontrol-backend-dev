package com.supercontrol.backend.dto;

import lombok.Data;

@Data
public class GameStartRequest {
    private Long userId;
    private Long machineId;
}
