package com.kronos.runtime.taskmanager;

import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.tasks.TaskOperatorEventGateway;
import com.kronos.runtime.execution.Environment;

/** */
public class RuntimeEnvironment implements Environment {
    private final TaskOperatorEventGateway operatorEventGateway;
    private final Source source;
    private final int operatorId;

    public RuntimeEnvironment(
            TaskOperatorEventGateway operatorEventGateway, Source source, int operatorId) {
        this.operatorEventGateway = operatorEventGateway;
        this.source = source;
        this.operatorId = operatorId;
    }

    @Override
    public int getExecutionId() {
        return 0;
    }

    @Override
    public TaskOperatorEventGateway getOperatorCoordinatorEventGateway() {
        return operatorEventGateway;
    }

    @Override
    public int getOperatorId() {
        return operatorId;
    }

    public Source source() {
        return source;
    }
}
