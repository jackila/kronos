package com.kronos.runtime.taskmanager;

import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.tasks.TaskOperatorEventGateway;
import com.kronos.runtime.execution.Environment;

/**
 * @Author: jackila
 * @Date: 23:06 2022-10-19
 */
public class RuntimeEnvironment implements Environment {
    private final TaskOperatorEventGateway operatorEventGateway;
    private final Source source;

    public RuntimeEnvironment(TaskOperatorEventGateway operatorEventGateway,Source source) {
        this.operatorEventGateway = operatorEventGateway;
        this.source = source;
    }


    @Override
    public int getExecutionId() {
        return 0;
    }

    @Override
    public TaskOperatorEventGateway getOperatorCoordinatorEventGateway() {
        return operatorEventGateway;
    }

    public Source source() {
        return source;
    }
}
