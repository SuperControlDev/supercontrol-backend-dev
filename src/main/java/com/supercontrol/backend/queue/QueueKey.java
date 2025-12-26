package com.supercontrol.backend.queue;

public class QueueKey {

    public static String queueZset(Long machineId) {
        return "queue:z:" + machineId;
    }

    public static String queueEntry(String queueEntryId) {
        return "queue:entry:" + queueEntryId;
    }

    public static String turn(Long machineId) {
        return "queue:turn:" + machineId;
    }
}
