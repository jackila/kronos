package com.kronos.runtime.executiongraph;

import com.kronos.runtime.message.Acknowledge;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.taskexecutor.TaskExecutorGateway;
import com.kronos.utils.FlinkException;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: jackila
 * @Date: 22:12 2022-10-17
 */
public class LocalTaskManagerGateway implements TaskExecutorOperatorEventGateway {

    private TaskExecutorGateway taskExecutorGateway;
    private final int jobMasterId = 0;

    public LocalTaskManagerGateway(TaskExecutorGateway taskExecutorGateway) {
        this.taskExecutorGateway = taskExecutorGateway;
    }

    @Override
    public void dispatchOperatorEvent(int operator,
                                      OperatorEvent event) throws FlinkException {
        taskExecutorGateway.dispatchOperatorEvent(operator, event);
    }
}
