package com.supercontrol.backend.dto;

import com.supercontrol.backend.domain.Machine;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineResponse {

    private String machineId;
    private String name;
    private String description;
    private int price;
    private int viewers;
    private boolean isLive;
    private String status;
    private String thumbnailUrl;

    public static MachineResponse from(Machine m) {
        return MachineResponse.builder()
                // .machineId(m.getMachineId())
                // .name(m.getName())
                // .description(m.getDescription())
                // .price(m.getPrice())
                // .viewers(m.getViewers())
                // .isLive(m.isLive())
                // .status(m.getStatus().name().toLowerCase())
                // .thumbnailUrl(m.getThumbnailUrl())
                .build();
    }
}
