package com.kronos.jobgraph.physic;

import com.google.common.annotations.VisibleForTesting;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.Record;
import com.kronos.cdc.data.sink.RecordSet;
import com.kronos.jobgraph.common.RestClientFactoryImpl;
import com.kronos.jobgraph.physic.disruptor.ProcessorInput;
import com.kronos.jobgraph.physic.disruptor.ProcessorOutput;
import com.kronos.jobgraph.physic.operator.db.DataWarehouseManager;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import com.kronos.jobgraph.physic.operator.handler.BackStageTableHandler;
import com.kronos.jobgraph.physic.operator.handler.ControllerHandler;
import com.kronos.jobgraph.physic.operator.handler.ModifyWareHouseHandler;
import com.kronos.jobgraph.physic.operator.handler.SinkerHandler;
import com.kronos.jobgraph.physic.operator.handler.StageType;
import com.kronos.jobgraph.physic.operator.handler.prepare.FrontStageTableHandler;
import com.kronos.jobgraph.physic.operator.handler.prepare.MiddleStageTableHandler;
import com.kronos.jobgraph.physic.operator.handler.sink.ESSinkFunctionHandler;
import com.kronos.jobgraph.table.CatalogManager;
import com.kronos.jobgraph.table.database.ElasticsearchCatalogDatabase;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.kronos.ElasticsearchSink;
import org.kronos.base.ElasticsearchSinkBase;
import org.kronos.connector.SinkFunction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @Author: jackila
 * @Date: 16:53 2022-12-14
 */
public class JoinGraphOperator<T extends Record> {
    private RingBuffer<StreamRecord<DiffStageRecords>> sourceRingBuffer;
    private RingBuffer<StreamRecord<DiffStageRecords>> sinkerRingBuffer;
    private JoinPhysicalGraph graph;
    private DataWarehouseManager warehouseManager;

    private List<EventProcessor> consumerRepository;

    private ElasticsearchCatalogDatabase catalogDatabase;

    // can not change
    private static final int MODIFY_SINGLE = 1;
    private static final int CONTROLLER_PARALLEL = 1;
    private static final int SINKER_PARALLEL = 1;

    public JoinGraphOperator(RingBuffer<StreamRecord<DiffStageRecords>> sourceRingBuffer,
                             RingBuffer<StreamRecord<DiffStageRecords>> sinkerRingBuffer,
                             JoinPhysicalGraph graph,
                             DataWarehouseManager warehouseManager
    ) {
        this.graph = graph;
        this.sourceRingBuffer = sourceRingBuffer;
        this.sinkerRingBuffer = sinkerRingBuffer;
        this.warehouseManager = warehouseManager;
        this.consumerRepository = new ArrayList<>();
        this.catalogDatabase = (ElasticsearchCatalogDatabase)CatalogManager.getInstance().findSinkerCatalog();
    }

    public void createOperator() {
        // create save record operator
        ProcessorInput nextInput = createModifyOperator();
        // filter operator
        // controller operator
        nextInput = appendControllerOperator(nextInput);
        //join operator
        nextInput = appendTaskOperator((type) -> chooseHandler(type), nextInput);
        //end and add to queue(or sinker)
        SinkFunction sink = createSinkFunction();
        appendSinkerOperator(nextInput,sink);
    }

    private SinkFunction createSinkFunction() {


        // use a ElasticsearchSink.Builder to create an ElasticsearchSink
        ElasticsearchSink.Builder<RecordSet> esSinkBuilder = new ElasticsearchSink.Builder<RecordSet>(
                catalogDatabase.httpHosts(),
                new ESSinkFunctionHandler(catalogDatabase,graph)
        );

        esSinkBuilder.setRestClientFactory(new RestClientFactoryImpl(catalogDatabase.getUsername(),
                                                                     catalogDatabase.getPassword()));

        // configuration for the bulk requests; this instructs the sink to emit after every element, otherwise they
        // would be buffered
        if (catalogDatabase.getBulkAction()> 0) {
            esSinkBuilder.setBulkFlushMaxActions(catalogDatabase.getBulkAction());
        }

        if (catalogDatabase.getBulkSizeMb()> 0) {
            esSinkBuilder.setBulkFlushMaxSizeMb(catalogDatabase.getBulkSizeMb());
        }

        if (catalogDatabase.getBulkIntervalMs()> 0) {
            esSinkBuilder.setBulkFlushInterval(catalogDatabase.getBulkIntervalMs());
        }

        /**
         * 是否开启异常重试机制
         */
        esSinkBuilder.setBulkFlushBackoff(catalogDatabase.isBackoffEnable());

        String backoffType = catalogDatabase.getBackoffType();
        if (StringUtils.equals("CONSTANT", backoffType) || StringUtils.isBlank(backoffType)) {
            esSinkBuilder.setBulkFlushBackoffType(ElasticsearchSinkBase.FlushBackoffType.CONSTANT);
        } else if (StringUtils.equals("EXPONENTIAL", backoffType)) {
            esSinkBuilder.setBulkFlushBackoffType(ElasticsearchSinkBase.FlushBackoffType.EXPONENTIAL);
        } else {
            throw new RuntimeException("backoff type not support this type:" + backoffType);
        }
        esSinkBuilder.setBulkFlushBackoffDelay(catalogDatabase.getBackoffDelay());
        esSinkBuilder.setBulkFlushBackoffRetries(catalogDatabase.getBackoffRetries());

        //esSinkBuilder.setFailureHandler(new CustomActionRequestFailureHandler());

        return esSinkBuilder.build();
    }

    private ProcessorInput createModifyOperator() {
        ModifyWareHouseHandler modifyWareHouseHandler = new ModifyWareHouseHandler();
        return createOperator(new ProcessorInput(), MODIFY_SINGLE, modifyWareHouseHandler);
    }

    private ProcessorInput appendControllerOperator(ProcessorInput nextInput) {
        ControllerHandler handler = new ControllerHandler(graph);
        return createOperator(nextInput, CONTROLLER_PARALLEL, handler);
    }

    private ProcessorInput appendSinkerOperator(ProcessorInput nextInput,
                                                SinkFunction sink) {
        SinkerHandler handler = new SinkerHandler(sink);
        return createOperator(nextInput, SINKER_PARALLEL, handler, sinkerRingBuffer);
    }

    public ProcessorInput appendTaskOperator(Function<StageType, AbstractTableTransformerHandler> chooseHandler,
                                             ProcessorInput input) {
        //font stage
        ProcessorInput nextInput = createFontOperator(chooseHandler.apply(StageType.FRONT), input);
        //middle stage
        createMiddleStage(graph.getRoot(), nextInput, chooseHandler.apply(StageType.MIDDLE));
        //end stage
        ProcessorInput chainEndInput = createBackOperator(chooseHandler.apply(StageType.BACK));
        return chainEndInput;
    }

    @SneakyThrows
    private void createMiddleStage(TPhysicalNode root,
                                   ProcessorInput input,
                                   AbstractTableTransformerHandler handler) {
        handler.setNode(root);
        this.createOperator(root, input, handler, sourceRingBuffer);
    }

    /**
     * 需要对逻辑树产生倒序算子图
     *
     * @param handler
     * @param input
     */
    private ProcessorInput createFontOperator(AbstractTableTransformerHandler handler,
                                              ProcessorInput input) {

        return innerFrontCreated(graph, graph.getRoot(), handler, input);
    }

    @VisibleForTesting
    @SneakyThrows
    public ProcessorInput innerFrontCreated(JoinPhysicalGraph graph,
                                            TPhysicalNode node,
                                            AbstractTableTransformerHandler handler,
                                            ProcessorInput originInput) {
        if (node.getNodes() == null || node.getNodes().isEmpty()) {
            if (node == graph.getRoot()) {
                return originInput;
            } else {
                AbstractTableTransformerHandler clone = handler.clone(node);
                clone.setNode(node);
                return createOperator(node, originInput, clone, sourceRingBuffer);
            }
        }

        ProcessorInput inputs = new ProcessorInput();
        for (TPhysicalNode child : node.getNodes()) {
            ProcessorInput processorInput = innerFrontCreated(graph, child, handler, originInput);
            inputs.addSource(processorInput);
        }

        if (node == graph.getRoot()) {
            return inputs;
        } else {
            return createOperator(node, inputs, handler.clone(node), sourceRingBuffer);
        }
    }

    @VisibleForTesting
    @SneakyThrows
    public ProcessorInput createBackOperator(AbstractTableTransformerHandler handler) {
        TPhysicalNode root = graph.getRoot();
        Deque<TPhysicalNode> stack = new ArrayDeque<>();
        stack.push(root);
        ProcessorInput sinkerInput = new ProcessorInput();
        while (!stack.isEmpty()) {
            TPhysicalNode head = stack.poll();
            // default root is not init by this stage
            ProcessorInput nextInput = head == root ? root.getOutput().convertTo() : createOperator(head, null,
                                                                                                    handler.clone(head),
                                                                                                    sinkerRingBuffer);
            List<TPhysicalNode> childs = head.getNodes();
            if (childs == null || childs.size() == 0) {
                sinkerInput.addSource(nextInput);
                continue;
            }
            for (TPhysicalNode child : childs) {
                child.setInput(nextInput);
                stack.offer(child);
            }
        }

        return sinkerInput;
    }

    @VisibleForTesting
    public ProcessorInput createOperator(TPhysicalNode head,
                                         ProcessorInput input,
                                         AbstractTableTransformerHandler handler) {
        return createOperator(head, input, handler, null);
    }

    private ProcessorInput createOperator(TPhysicalNode head,
                                          ProcessorInput input,
                                          AbstractTableTransformerHandler handler,
                                          RingBuffer<StreamRecord<DiffStageRecords>> ring) {
        if (ring == null) {
            ring = sourceRingBuffer;
        }
        // 准备线程池
        ExecutorService executors = Executors.newFixedThreadPool(head.getParallel());

        if (input == null) {
            input = head.getInput();
        }

        SequenceBarrier barrier = ring.newBarrier(input.getSource());
        handler.setWareHouseManager(warehouseManager);
        BatchEventProcessor<StreamRecord> processor = new BatchEventProcessor<>(ring, barrier, handler);
        consumerRepository.add(processor);
        processor.setExceptionHandler(new FatalExceptionHandler());
        ProcessorOutput out = new ProcessorOutput(processor.getSequence());
        head.setOutput(out);
        ring.addGatingSequences(out.output());

        // 在一个独立线程中取事件并消费
        executors.submit(processor);
        return out.convertTo();
    }

    private ProcessorInput createOperator(ProcessorInput nextInput,
                                          int parallel,
                                          AbstractTableTransformerHandler handler,
                                          RingBuffer<StreamRecord<DiffStageRecords>> ringBuffer
    ) {
        // 准备线程池
        ExecutorService executors = Executors.newFixedThreadPool(parallel);

        SequenceBarrier barrier = ringBuffer.newBarrier(nextInput.getSource());
        handler.setWareHouseManager(warehouseManager);
        BatchEventProcessor<StreamRecord<DiffStageRecords>> processor = new BatchEventProcessor<>(ringBuffer,
                                                                                                  barrier, handler);
        processor.setExceptionHandler(new FatalExceptionHandler());
        consumerRepository.add(processor);
        ProcessorOutput out = new ProcessorOutput(processor.getSequence());
        ringBuffer.addGatingSequences(out.output());

        // 在一个独立线程中取事件并消费
        executors.submit(processor);
        return out.convertTo();
    }

    @SneakyThrows
    private ProcessorInput createOperator(ProcessorInput nextInput,
                                          int parallel,
                                          AbstractTableTransformerHandler handler) {
        return createOperator(nextInput, parallel, handler, sourceRingBuffer);
    }

    private AbstractTableTransformerHandler chooseHandler(StageType stage) {
        AbstractTableTransformerHandler handler = null;
        switch (stage) {
            case MODIFY_WAREHOUSE:
                handler = new ModifyWareHouseHandler();
                break;
            case CONTROLLER:
                handler = new ControllerHandler(graph);
                break;
            case FRONT:
                handler = new FrontStageTableHandler();
                break;
            case MIDDLE:
                MiddleStageTableHandler middleStageTableHandler = new MiddleStageTableHandler();
                middleStageTableHandler.setSinkerRingBuffer(sinkerRingBuffer);
                handler = middleStageTableHandler;
                break;
            case BACK:
                handler = new BackStageTableHandler();
                break;
            case SINKER:
                handler = new SinkerHandler();
                break;
            default:
                new RuntimeException("not support the type " + stage.name());
        }
        return handler;
    }

    public List<EventProcessor> getConsumerRepository() {
        return consumerRepository;
    }
}
