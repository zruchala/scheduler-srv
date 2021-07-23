package org.zrc.service.scheduler;

import java.util.Map;

public class StdOutboxPublisher implements OutboxPublisher {
    @Override
    public void publish(String payload, Map<String, String> headers) {
        System.out.println("Published message: ");
        System.out.printf("Payload: %s%n", payload);
        System.out.printf("Payload: %s%n", payload);
    }
}
