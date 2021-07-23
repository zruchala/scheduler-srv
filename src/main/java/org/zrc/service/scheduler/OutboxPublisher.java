package org.zrc.service.scheduler;

import java.util.Map;

public interface OutboxPublisher {

    void publish(String payload, Map<String, String> headers);

}
