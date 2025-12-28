package com.supercontrol.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservedCheckResponse {

    private String status;
    private String startToken;

    public static ReservedCheckResponse ready(String token) {
        return new ReservedCheckResponse("READY", token);
    }

    public static ReservedCheckResponse waiting() {
        return new ReservedCheckResponse("WAITING", null);
    }
}