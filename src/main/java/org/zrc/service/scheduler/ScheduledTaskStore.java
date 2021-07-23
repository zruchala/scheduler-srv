package org.zrc.service.scheduler;

import org.zrc.service.scheduler.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskStore {

    private final Map<String, List<ScheduledFuture<?>>> tasks = new ConcurrentHashMap<>();

    private final ThreadPoolTaskScheduler taskScheduler;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(value = ScheduleTaskRequestEvent.class, fallbackExecution = true)
    public void scheduleTask(ScheduleTaskRequestEvent scheduleTaskRequestEvent) {
        var task = scheduleTaskRequestEvent.getSource();
        task.getValidScheduleTime().ifPresentOrElse(
            startTime -> {
                log.info("Scheduling task at {}: task={} within the next hour", task.getScheduledAt(), task);
                final var taskId = task.getId();
                var scheduledAt = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
                var scheduled = taskScheduler.schedule(
                        () -> applicationEventPublisher.publishEvent(new ExecuteTaskEvent(taskId)),
                        scheduledAt);
                var list = tasks.getOrDefault(task.getOriginId(), new ArrayList<>());
                list.add(scheduled);
                tasks.put(task.getOriginId(), list);
            },
            () -> log.warn("Request to schedule a task: {} with an invalid schedule time ({}) refused.", task, task.getScheduledAt()));
    }

    public void cancel(String originId) {
        if (tasks.containsKey(originId)) {
            log.info("Deleting scheduled tasks of originId: {}", originId);
            tasks.get(originId).forEach(scheduled -> {
                scheduled.cancel(false);
            });
            tasks.remove(originId);
        }
    }

    public void remove(Task task) {
        if (tasks.containsKey(task.getOriginId())) {
            var list = tasks.get(task.getOriginId());
            if (list.size() == 1) {
                tasks.remove(task.getOriginId());
            } else {
                list.remove(task);
            }
        }
    }

    public boolean hasTask(String originId) {
        return tasks.containsKey(originId);
    }
}
