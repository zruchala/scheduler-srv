package org.zrc.service.scheduler.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zrc.service.scheduler.SchedulerService;
import org.zrc.service.scheduler.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * An example of Client confirmation process.
 *
 * On receiving ClientCreated event the subsequent tasks are scheduled (verification reminders and delete action). These actions
 * are executed at the scheduled time unless the ClientConfirmed event is received.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SampleClientListener {

    private final SchedulerService schedulerService;
    private final ObjectMapper objectMapper;

    private final int expirationInMinutes = 20160;
    private final List<Integer> remindersInMinutes = List.of(1440, 4320);

    @StreamListener(value = Sink.INPUT)
    void listen(ClientCreated message) {
        log.info("Processing message: {}", message.getClientId());
        if (log.isDebugEnabled()) {
            log.debug("Processing message: {}", message);
        }

        var msg = new ClientDeletionRequest().setClientId(message.getClientId());
        var scheduledAt = message.getCreatedAt().plusMinutes(expirationInMinutes);
        var task = new Task()
                .setOriginId(originId(message.getClientId()))
                .setScheduledAt(scheduledAt)
                .setPayload(getPayload(msg));
        // the new invitation has been generated, drop the previous events for origin
        schedulerService.schedule(task, SchedulerService.CurrentTask.DELETE);

        remindersInMinutes.forEach(minutes -> {
            var reminder = new ClientReminderRequest().setClientId(message.getClientId());
            var reminderScheduledAt = message.getCreatedAt().plusMinutes(expirationInMinutes - minutes);
            var reminderTask = new Task()
                    .setOriginId(originId(message.getClientId()))
                    .setScheduledAt(reminderScheduledAt)
                    .setPayload(getPayload(reminder));
            schedulerService.schedule(reminderTask);
        });
    }

    @StreamListener(value = Sink.INPUT)
    void listen(ClientConfirmed message) {
        log.info("Processing message: {}", message.getClientId());
        if (log.isDebugEnabled()) {
            log.debug("Processing message: {}", message);
        }
        // cancel all remaining tasks for ClientCreated event.
        schedulerService.cancel(originId(message.getClientId()));
    }

    private String originId(String clientId) {
        return Strings.join(List.of("ClientCreated", clientId), '_');
    }

    private String getPayload(Object payload) {
        try {
            return payload instanceof String ?
                    (String) payload : objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
