package com.supercontrol.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrabResultDto {
    private boolean success;
    private String reason;
    private long timestamp;
}
