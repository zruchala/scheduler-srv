package com.flexvoucher.scheduler;

import com.flexvoucher.scheduler.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Set;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    void deleteByOriginId(String originId);

    Set<Task> findByOriginId(String originId);

    static Specification<Task> hasScheduledAtBefore(LocalDateTime before) {
        return (root, query, builder) -> builder.lessThan(root.get("scheduledAt"), before);
    }
}
