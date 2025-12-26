package com.supercontrol.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueStatusResponse {

    private Integer position; // 대기 순번 (null이면 큐 없음)
    private String state; // waiting | ready | playing
    private boolean canStart; // 시작 버튼 활성화 여부
    private String startToken; // ready 상태일 때만
    private Long readyExpiresAt; // ready 만료 시각 (FE 카운트다운용)
}
