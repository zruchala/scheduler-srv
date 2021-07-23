package org.zrc.service.scheduler;

public class TaskNotFound extends RuntimeException{

    public TaskNotFound(String msg) {
        super(msg);
    }

}
