package com.supercontrol.backend.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueGateResult {

    private boolean canStart;
    private String startToken;

    public static QueueGateResult start(String token) {
        return new QueueGateResult(true, token);
    }

    public static QueueGateResult waiting() {
        return new QueueGateResult(false, null);
    }
}