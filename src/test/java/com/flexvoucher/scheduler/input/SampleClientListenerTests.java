package com.flexvoucher.scheduler.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexvoucher.scheduler.SchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "expirationInMinutes=20160",
        "remindersInMinutes=1440, 4320"})
public class SampleClientListenerTests {

    @TestConfiguration
    static class Context  {
        @MockBean SchedulerService schedulerService;
        @MockBean ObjectMapper objectMapper;
        @Bean public SampleClientListener sampleClientListener() { return new SampleClientListener(schedulerService, objectMapper); }
        @Bean public ConversionService conversionService() { return new DefaultConversionService(); }
    }

    @Autowired SampleClientListener sampleClientListener;
    @Autowired SchedulerService schedulerService;
    @Autowired ObjectMapper objectMapper;

    @Value("${expirationInMinutes}")
    private int expirationInMinutes;

    @Value("${remindersInMinutes}")
    private List<Integer> remindersInMinutes;

    @Test
    public void test() {
        var date = LocalDateTime.of(2020, 1, 1, 0, 0);
        var clientCreated = new ClientCreated().setClientId("123456");
        clientCreated.setCreatedAt(date);

        sampleClientListener.listen(clientCreated);

        verify(schedulerService, times(1)).schedule(argThat(task ->
                date.plusMinutes(expirationInMinutes).compareTo(task.getScheduledAt()) == 0
        ), eq(SchedulerService.CurrentTask.DELETE));

        for(var reminder : remindersInMinutes) {
            verify(schedulerService, times(1)).schedule(argThat(task ->
                    date.plusMinutes(expirationInMinutes).minusMinutes(reminder).compareTo(task.getScheduledAt()) == 0
            ));
        }
    }
}
