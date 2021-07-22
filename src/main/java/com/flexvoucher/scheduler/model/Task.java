package com.flexvoucher.scheduler.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Entity
@Table(name = "task")
@TypeDefs({
    @TypeDef(name = "hstore", typeClass = HstoreType.class)
})
@Getter
@Setter
@ToString
@Accessors(chain = true)
@DynamicUpdate
@Slf4j
public class Task {

    public enum Period {
        NONE, // not periodic task or last pass. It should be deleted after execution.
        WEEKLY, // always 7 days
        BIWEEKLY, // always 14 days
        MONTHLY // next month at the same day
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * What created the task (eg. identifier of the initiating event)
     */
    @Column(nullable = false)
    private String originId;

    @Type(type = "hstore")
    @Column(columnDefinition = "hstore", updatable = false)
    private Map<String, String> headers = new HashMap<>();

    @Type(type = "hstore")
    @Column(columnDefinition = "hstore", updatable = false)
    private Map<String, String> properties = new HashMap<>();

    @Column(columnDefinition = "text", updatable = false)
    private String payload;

    @CreationTimestamp
    @Column(insertable = false, updatable = false,
            columnDefinition = "timestamp not null default (now() at time zone 'utc')")
    private Timestamp createdAt;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Period period = Period.NONE;

    /**
     * The acceptable delay in seconds. If not set, the task never expires and will be performed
     * at the earliest opportunity.
     */
    @Column
    private Long acceptableDelay;

    public Optional<LocalDateTime> getValidScheduleTime() {
        var now = LocalDateTime.now();
        LocalDateTime validScheduleTime = scheduledAt;
        if (scheduledAt.compareTo(now) < 0) {
            // the scheduledAt has passed already ...
            if (acceptableDelay == null || scheduledAt.plusSeconds(acceptableDelay).compareTo(now) > 0) {
                validScheduleTime = now.plusSeconds(5);
            } else {
                validScheduleTime = null;
            }
        }
        return Optional.ofNullable(validScheduleTime);
    }

    /**
     * Request to reschedule task to the next period.
     * @return true if was rescheduled, false if it is not applicable. In former case the task should get deleted.
     */
    public boolean rescheduleToNextPeriod(ObjectMapper objectMapper) {
        if (period == Period.NONE) {
            return false;
        }

        switch (period) {
            case WEEKLY:
                setScheduledAt(getScheduledAt().plusWeeks(1));
                log.info("Rescheduled task in the next week at: {}, {}", getScheduledAt(), this);
                break;
            case BIWEEKLY:
                setScheduledAt(getScheduledAt().plusWeeks(2));
                log.info("Rescheduled task in two weeks at: {}, {}", getScheduledAt(), this);
                break;
            case MONTHLY:
                setScheduledAt(getScheduledAt().plusMonths(1));
                log.info("Rescheduled task on the next month at: {}, {}", getScheduledAt(), this);
                break;
            default:
                throw new IllegalStateException("Unsupported period for rescheduling: " + period);
        }
        return true;
    }

}
