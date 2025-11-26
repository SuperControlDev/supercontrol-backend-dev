package com.supercontrol.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineListResponse {
    private List<MachineResponse> machines;
}
