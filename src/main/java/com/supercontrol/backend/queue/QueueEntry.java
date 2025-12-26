package com.supercontrol.backend.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueEntry {

    private final String queueEntryId;
    private final Long userId;
    private final Long machineId;
    private final long joinedAt;
}
