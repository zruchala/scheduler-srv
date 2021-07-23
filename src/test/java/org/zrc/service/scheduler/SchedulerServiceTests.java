package org.zrc.service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zrc.service.scheduler.config.SchedulerConfig;
import org.zrc.service.scheduler.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@AutoConfigureJsonTesters
@Import(SchedulerConfig.class)
public class SchedulerServiceTests {

    @TestConfiguration
    static class Context {
        @MockBean TaskRepository taskRepository;
        @MockBean OutboxPublisher outboxPublisher;
        @Autowired ApplicationEventPublisher applicationEventPublisher;
        @Autowired ObjectMapper objectMapper;
        @Autowired ScheduledTaskStore scheduledTaskStore;
        @Bean
        public SchedulerService schedulerService() {
            return new SchedulerService(taskRepository, scheduledTaskStore, applicationEventPublisher, outboxPublisher, objectMapper);
        }
    }

    @Autowired SchedulerService schedulerService;
    @Autowired TaskRepository taskRepository;
    @Autowired ScheduledTaskStore scheduledTaskStore;

    @Test
    public void testEmptyTasks() {
        when(taskRepository.findAll((Specification)any())).thenReturn(List.of());
        schedulerService.bulkScheduling();
    }

    @Test
    public void testTasks1() {
        var task = new Task().setOriginId("id1").setScheduledAt(LocalDateTime.now().plusMinutes(59));
        when(taskRepository.findAll((Specification)any())).thenReturn(List.of(task));
        schedulerService.bulkScheduling();
        assertTrue(scheduledTaskStore.hasTask("id1"));
    }

    @Test
    public void schedule1() {
        var task = new Task().setOriginId("s1").setScheduledAt(LocalDateTime.now().plusMinutes(59));
        schedulerService.bulkScheduling(); // make sure SchedulerService#nextPeriod is initialized
        schedulerService.schedule(task);
        assertTrue(scheduledTaskStore.hasTask("s1"));
    }

    @Test
    public void schedule2() {
        var task = new Task().setOriginId("s2").setScheduledAt(LocalDateTime.now().plusMinutes(61));
        schedulerService.bulkScheduling();
        schedulerService.schedule(task);
        assertFalse(scheduledTaskStore.hasTask("s2"));
    }
}
