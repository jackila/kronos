package com.kronos.runtime.executiongraph;

import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.jobmaster.JobMaster;
import com.kronos.runtime.operators.coordination.OperatorCoordinatorHolder;
import com.kronos.runtime.source.coordinator.OperatorCoordinator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @Author: jackila
 * @Date: 17:40 2022-10-16
 */
public class DefaultExecutionGraph implements ExecutionGraph {

    private ExecutionJobVertex sourceVertex;

    public DefaultExecutionGraph(Source source,
                                 JobMaster master){
        try {
            sourceVertex = new ExecutionJobVertex(source,master);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public OperatorCoordinator.Provider getCoordinatorProvider(
            String operatorName,
            int operatorID) {
        return sourceVertex.getCoordinatorProvider(operatorName, operatorID);
    }

    public OperatorCoordinatorHolder coordinatorHolder() {
        return sourceVertex.coordinatorHolder();
    }

    @Override
    public List<Future> runAll(JoinPhysicalGraph graph) {
        List<Future> status = new ArrayList<>();
        for (Execution execution : sourceVertex.getAllExecution()) {
            try {
                status.addAll(execution.getTask().execute(graph));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return status;
    }

}
