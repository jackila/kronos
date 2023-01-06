package com.kronos.runtime.taskexecutor;

import com.kronos.jobgraph.tasks.TaskOperatorEventGateway;
import com.kronos.runtime.jobmaster.JobMasterOperatorEventGateway;
import com.kronos.runtime.operators.coordination.OperatorEvent;

/** */
public class LocalTaskOperatorEventGateway implements TaskOperatorEventGateway {
    private final JobMasterOperatorEventGateway rpcGateway;

    private int taskId;

    public LocalTaskOperatorEventGateway(JobMasterOperatorEventGateway rpcGateway, int taskId) {
        this.rpcGateway = rpcGateway;
        this.taskId = taskId;
    }

    @Override
    public void sendOperatorEventToCoordinator(int operator, OperatorEvent event) {
        rpcGateway.sendOperatorEventToCoordinator(taskId, operator, event);
    }
}
