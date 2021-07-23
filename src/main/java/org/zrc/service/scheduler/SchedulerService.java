package org.zrc.service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zrc.service.scheduler.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    public enum CurrentTask {
        NOOP, // do nothing. Let them as they are.
        DELETE, // delete tasks which are not being executed at the moment.
        CANCEL_REPEAT // do not stop already scheduled task but cancel repeating them in the future.
    }

    private final TaskRepository taskRepository;
    private final ScheduledTaskStore scheduledTaskStore;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OutboxPublisher outboxPublisher;
    private final ObjectMapper objectMapper;


    private volatile LocalDateTime nextPeriod;

    /**
     * After 5s initialDelay it schedules tasks that should be executed within the next hour.
     */
    @Scheduled(initialDelay = 1000 * 5, fixedRate = 1000 * 60 * 60)
    void bulkScheduling() {
        nextPeriod = LocalDateTime.now().plusHours(1);
        log.info("Initiate bulk tasks scheduling for tasks ended before {}", nextPeriod);
        Specification<Task> spec = Specification.where(TaskRepository.hasScheduledAtBefore(nextPeriod));
        var tasks = taskRepository.findAll(spec);
        AtomicInteger taskCounter = new AtomicInteger(tasks.size());
        tasks.forEach(task -> task.getValidScheduleTime().ifPresentOrElse(
            time -> applicationEventPublisher.publishEvent(new ScheduleTaskRequestEvent(task)),
            () -> {
                log.warn("The task scheduledAt {}, with acceptableDelay: {} expired. It's going to be removed. Task: {}",
                        task.getScheduledAt(), task.getAcceptableDelay(), task);
                applicationEventPublisher.publishEvent(new TaskEndedEvent(task));
                taskCounter.getAndDecrement();
            }));
        log.info("Bulk scheduled {} tasks", taskCounter);
    }

    public void schedule(Task task) {
        schedule(task, CurrentTask.NOOP);
    }

    public void schedule(Task task, CurrentTask currentTask) {
        if (!isScheduledForTheFuture(task.getScheduledAt())) {
            throw new IllegalArgumentException("Task task#scheduleAt must be a future date");
        }

        var originId = task.getOriginId();
        switch (currentTask) {
            case DELETE:
                cancel(originId);
                break;
            case CANCEL_REPEAT:
                taskRepository.findByOriginId(originId).forEach(
                        repeatingTask -> task.setPeriod(Task.Period.NONE));
                break;
        }

        log.info("Creating task: {} ", task);
        taskRepository.save(task);
        if (nextPeriod != null && task.getScheduledAt().compareTo(nextPeriod) < 0) {
            applicationEventPublisher.publishEvent(new ScheduleTaskRequestEvent(task));
        }
    }

    private boolean isScheduledForTheFuture(LocalDateTime scheduledAt) {
        return LocalDateTime.now().plusSeconds(1).compareTo(scheduledAt) < 1;
    }

    @EventListener(ExecuteTaskEvent.class)
    public void execute(ExecuteTaskEvent executeTaskEvent) {
        var taskId = executeTaskEvent.getSource();
        var task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFound(String.format("No task with id %s exists", taskId)));
        log.info("Executing scheduled task {}", task);
        outboxPublisher.publish(task.getPayload(), task.getHeaders());
        applicationEventPublisher.publishEvent(new TaskEndedEvent(task));
        scheduledTaskStore.remove(task);
    }

    public void cancel(String originId) {
        log.info("Cancelling tasks of origin: {}", originId);
        taskRepository.deleteByOriginId(originId);
        scheduledTaskStore.cancel(originId);
    }

    @EventListener(value = TaskEndedEvent.class)
    public void deleteOrReschedule(TaskEndedEvent taskEndedEvent) {
        var task = taskEndedEvent.getSource();
        if (!task.rescheduleToNextPeriod(objectMapper)) {
            log.info("Deleting the executed task: {}", task);
            taskRepository.deleteById(task.getId());
        }
    }
}

