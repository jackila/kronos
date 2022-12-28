package com.kronos.runtime.executiongraph;

import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.runtime.operators.coordination.OperatorCoordinatorHolder;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @Author: jackila
 * @Date: 17:40 2022-10-16
 */
public interface ExecutionGraph {
    public OperatorCoordinatorHolder coordinatorHolder();

    List<Future> runAll(JoinPhysicalGraph graph);
}
