package com.kronos.runtime.executiongraph;

import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.StreamTask;
import com.kronos.runtime.execution.Environment;
import com.kronos.runtime.jobmaster.JobMaster;
import com.kronos.runtime.operators.coordination.OperatorCoordinatorHolder;
import com.kronos.runtime.source.coordinator.OperatorCoordinator;
import com.kronos.runtime.source.coordinator.SourceCoordinatorProvider;
import com.kronos.runtime.taskexecutor.LocalTaskOperatorEventGateway;
import com.kronos.runtime.taskmanager.RuntimeEnvironment;
import com.kronos.runtime.tasks.SourceOperatorStreamTask;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jackila
 * @Date: 21:56 2022-10-17
 */
public class ExecutionJobVertex {
    private String name = "";
    private int parallelism = 1;
    private int maxParallelism = 1;

    private Map<Integer, Execution> taskExecutions = new HashMap<>();

    private final Source source;
    private final OperatorCoordinatorHolder coordinatorHolder;

    public ExecutionJobVertex(Source source,
                              JobMaster master,
                              int operatorId) throws Exception {

        this.source = source;
        OperatorCoordinator.Provider provider = getCoordinatorProvider("source Operator", operatorId);
        this.coordinatorHolder = OperatorCoordinatorHolder.create(
                provider, this);

        for (int index = 0; index < parallelism; index++) {
            Environment env = new RuntimeEnvironment(new LocalTaskOperatorEventGateway(master, index), source);
            StreamTask task = new SourceOperatorStreamTask(index, env);
            taskExecutions.put(index, new Execution(task));
        }
    }

    public OperatorCoordinator.Provider getCoordinatorProvider(
            String operatorName,
            int operatorID) {
        return new SourceCoordinatorProvider<>(
                operatorName, operatorID, source);
    }

    public OperatorCoordinatorHolder coordinatorHolder() {
        return coordinatorHolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public int getMaxParallelism() {
        return maxParallelism;
    }

    public void setMaxParallelism(int maxParallelism) {
        this.maxParallelism = maxParallelism;
    }

    public Map<Integer, Execution> getTaskExecutions() {
        return taskExecutions;
    }

    public void setTaskExecutions(Map<Integer, Execution> taskExecutions) {
        this.taskExecutions = taskExecutions;
    }

    public Execution[] getAllExecution() {
        return taskExecutions.values().toArray(new Execution[]{});
    }
}
