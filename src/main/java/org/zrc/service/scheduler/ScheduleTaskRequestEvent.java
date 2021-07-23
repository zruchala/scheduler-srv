package org.zrc.service.scheduler;

import org.zrc.service.scheduler.model.Task;
import org.springframework.context.ApplicationEvent;

public class ScheduleTaskRequestEvent extends ApplicationEvent {

    public ScheduleTaskRequestEvent(Task task) {
        super(task);
    }

    @Override
    public Task getSource() {
        return (Task) super.getSource();
    }
}
