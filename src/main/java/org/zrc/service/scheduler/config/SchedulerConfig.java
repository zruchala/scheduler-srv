package org.zrc.service.scheduler.config;

import org.zrc.service.scheduler.OutboxPublisher;
import org.zrc.service.scheduler.ScheduledTaskStore;
import org.zrc.service.scheduler.StdOutboxPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@Slf4j
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("TaskScheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @PostConstruct
    void postConstruct() {
        log.debug("Setting default zone to Etc/UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

    @Bean
    public ScheduledTaskStore scheduledTasks(ThreadPoolTaskScheduler threadPoolTaskScheduler, ApplicationEventPublisher publisher) {
        return new ScheduledTaskStore(threadPoolTaskScheduler, publisher);
    }

    @Bean
    public OutboxPublisher outboxPublisher() {
        return new StdOutboxPublisher();
    }
}
