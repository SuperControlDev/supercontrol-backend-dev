package com.supercontrol.backend.dto;

import lombok.Data;

@Data
public class GameStartRequest {
    private String userId;
    private Long machineId;
}
