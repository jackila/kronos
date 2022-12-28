package com.kronos.runtime.executiongraph;

import com.kronos.runtime.StreamTask;
import com.kronos.runtime.message.Acknowledge;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.utils.FlinkException;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: jackila
 * @Date: 22:08 2022-10-17
 */
public class Execution {
    private final int subTaskIndex = 0;
    private TaskExecutorOperatorEventGateway taskManagerGateway;

    private StreamTask task;

    public Execution(StreamTask task) {
        this.taskManagerGateway = new LocalTaskManagerGateway(task);
        this.task = task;
    }

    public void sendOperatorEvent(OperatorEvent event) throws FlinkException {
        taskManagerGateway.dispatchOperatorEvent(0,event);
    }

    public int getSubTaskIndex() {
        return subTaskIndex;
    }

    public StreamTask getTask() {
        return task;
    }
}
