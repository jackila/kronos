package com.kronos.runtime.executiongraph;

import com.kronos.jobgraph.physic.JoinPhysicalGraph;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @Author: jackila
 * @Date: 17:40 2022-10-16
 */
public interface ExecutionGraph {
    List<ExecutionJobVertex> getAllVertices();

    List<Future> runAll(JoinPhysicalGraph graph);
}
