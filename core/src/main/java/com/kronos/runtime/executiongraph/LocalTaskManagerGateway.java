package com.kronos.runtime.executiongraph;

import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.taskexecutor.TaskExecutorGateway;
import com.kronos.utils.FlinkException;

/** */
public class LocalTaskManagerGateway implements TaskExecutorOperatorEventGateway {

    private TaskExecutorGateway taskExecutorGateway;
    private final int jobMasterId = 0;

    public LocalTaskManagerGateway(TaskExecutorGateway taskExecutorGateway) {
        this.taskExecutorGateway = taskExecutorGateway;
    }

    @Override
    public void dispatchOperatorEvent(int operator, OperatorEvent event) throws FlinkException {
        taskExecutorGateway.dispatchOperatorEvent(operator, event);
    }
}
