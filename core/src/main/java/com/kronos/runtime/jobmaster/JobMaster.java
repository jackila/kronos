package com.kronos.runtime.jobmaster;

import static com.kronos.utils.GraphUtil.convertToPhysicalGraph;

import com.kronos.jobgraph.logical.LogicalGraph;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.executiongraph.DefaultExecutionGraph;
import com.kronos.runtime.executiongraph.ExecutionGraph;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.scheduler.DefaultOperatorCoordinatorHandler;
import com.kronos.runtime.scheduler.OperatorCoordinatorHandler;
import com.kronos.utils.FlinkException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * job master负责coordinator的实例化与处理 source算子会与coordinator进行交互
 *
 * <p>1. 为何要继承runnable master是在主线程上执行，还是在独立的线程池执行需要待定，暂时不处理
 */
public class JobMaster implements Runnable, JobMasterOperatorEventGateway {
    private final OperatorCoordinatorHandler operatorCoordinatorHandler;

    private final ExecutionGraph executionGraph;

    public JobMaster(Source... source) {
        this.executionGraph = new DefaultExecutionGraph(this, source);
        this.operatorCoordinatorHandler = new DefaultOperatorCoordinatorHandler(executionGraph);
        operatorCoordinatorHandler.initializeOperatorCoordinators();
    }

    public void onStart() {
        operatorCoordinatorHandler.startAllOperatorCoordinators();
    }

    public void execute(LogicalGraph streamGraph) {
        this.onStart();
        JoinPhysicalGraph graph = convertToPhysicalGraph(streamGraph);
        List<Future> workStatus = deploy(graph);

        // where all task finished
        for (Future status : workStatus) {
            try {
                status.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        System.exit(0);
    }

    @Override
    public void run() {
        this.onStart();
    }

    @Override
    public void sendOperatorEventToCoordinator(int task, int operatorID, OperatorEvent event) {
        try {
            operatorCoordinatorHandler.deliverOperatorEventToCoordinator(task, operatorID, event);
        } catch (FlinkException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Future> deploy(JoinPhysicalGraph graph) {
        return executionGraph.runAll(graph);
    }
}
