package com.kronos.runtime.executiongraph;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.physic.JoinGraphOperator;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.db.DataWarehouseManager;
import com.kronos.jobgraph.physic.operator.db.MemoryWarehouseManager;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.StreamTask;
import com.kronos.runtime.jobmaster.JobMaster;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import lombok.SneakyThrows;

/** */
public class DefaultExecutionGraph implements ExecutionGraph {

    private List<ExecutionJobVertex> sourceVertexs;

    protected RingBuffer<StreamRecord<DiffStageRecords>> sourceRingBuffer;
    protected RingBuffer<StreamRecord<DiffStageRecords>> sinkRingBuffer;

    @SneakyThrows
    public DefaultExecutionGraph(JobMaster master, Source... sources) {
        sourceVertexs = new ArrayList<>();
        int operatorId = 0;
        for (Source source : sources) {
            sourceVertexs.add(new ExecutionJobVertex(source, master, operatorId));
            operatorId++;
        }

        sourceRingBuffer =
                RingBuffer.createMultiProducer(
                        new StreamTask.MessageFactory(), 1024 * 1024, new BlockingWaitStrategy());
        sinkRingBuffer =
                RingBuffer.createMultiProducer(
                        new StreamTask.MessageFactory(), 1024 * 1024, new BlockingWaitStrategy());
    }

    @Override
    public List<ExecutionJobVertex> getAllVertices() {
        return sourceVertexs;
    }

    @Override
    public List<Future> runAll(JoinPhysicalGraph graph) {
        List<Future> status = new ArrayList<>();
        for (ExecutionJobVertex sourceVertex : sourceVertexs) {
            for (Execution execution : sourceVertex.getAllExecution()) {
                status.addAll(execution.getTask().execute(graph, sourceRingBuffer));
            }
        }
        buildLocalOperator(graph);
        return status;
    }

    private void buildLocalOperator(JoinPhysicalGraph graph) {
        DataWarehouseManager warehouseManager =
                new MemoryWarehouseManager(); // new RocksDBWarehouseManager();
        JoinGraphOperator graphOperator =
                new JoinGraphOperator(sourceRingBuffer, sinkRingBuffer, graph, warehouseManager);
        graphOperator.createOperator();
    }
}
