package com.kronos.runtime.executiongraph;

import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import java.util.List;
import java.util.concurrent.Future;

/** */
public interface ExecutionGraph {
    List<ExecutionJobVertex> getAllVertices();

    List<Future> runAll(JoinPhysicalGraph graph);
}
