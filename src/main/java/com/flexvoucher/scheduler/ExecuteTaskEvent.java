package com.flexvoucher.scheduler;

import org.springframework.context.ApplicationEvent;

public class ExecuteTaskEvent extends ApplicationEvent {

    public ExecuteTaskEvent(Long taskId) {
        super(taskId);
    }

    @Override
    public Long getSource() {
        return (Long) super.getSource();
    }
}

