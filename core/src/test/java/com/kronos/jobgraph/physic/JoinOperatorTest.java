package com.kronos.jobgraph.physic;

import com.kronos.jobgraph.physic.disruptor.ProcessorInput;
import com.kronos.jobgraph.physic.operator.db.DataWarehouseManager;
import com.kronos.jobgraph.physic.operator.db.MemoryWarehouseManager;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import com.kronos.jobgraph.physic.operator.handler.StageType;
import com.kronos.jobgraph.table.ObjectPath;
import com.kronos.mock.MockEmptyHandler;
import com.kronos.mock.MockTPhysicalNode;
import com.kronos.mock.handler.MockBackStageTableHandler;
import com.kronos.mock.handler.MockFrontStageTableHandler;
import com.kronos.mock.handler.MockMiddleStageTableHandler;
import com.kronos.mock.handler.MockStageTableHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 2022-12-15 */
class JoinOperatorTest {

    JoinGraphOperator operator;
    RingBuffer<StreamRecord<List<String>>> ringBuffer;
    StreamRecordEventProducer producer;
    JoinPhysicalGraph graph;
    TPhysicalNode root;

    DataWarehouseManager warehouseManager;
    private MockTPhysicalNode endNode;
    int BUFFER_SIZE = 32;

    @BeforeEach
    public void start() {

        // 创建环形队列实例
        ringBuffer = RingBuffer.createSingleProducer(new StreamRecordEventFactory(), BUFFER_SIZE);
        root = mockTestBackStageTPhysicalNode();
        graph = new JoinPhysicalGraph(root);
        warehouseManager = new MemoryWarehouseManager();
        operator = new JoinGraphOperator(ringBuffer, ringBuffer, graph, warehouseManager);
        producer = new StreamRecordEventProducer(ringBuffer);
    }

    @AfterEach
    public void sendMessage() {
        int count = 0;
        while (count++ < 100) {
            producer.onData("A");
            producer.onData("B");
            producer.onData("C");
            producer.onData("D");
            producer.onData("E");
            producer.onData("F");
            producer.onData("G");
        }

        List<EventProcessor> consumerRepository = operator.getConsumerRepository();
        for (EventProcessor processor : consumerRepository) {
            processor.halt();
        }
    }

    /*
     *                               +---+
     *               +---------------+ A +---------------+
     *               |               +---+               |
     *               |                                   |
     *               |                                   |
     *               |                                   |
     *               |                                   |
     *             +-v-+                               +-v-+
     *             | B |                               | C |
     *   +---------+-+-+----------+                    +---+
     *   |           |            |
     *   |           |            |
     *   |           |            |
     *   |           |            |
     * +-v-+       +-v-+        +-v-+
     * | D |       | E |        | F |
     * +-+-+       +---+        +---+
     *   |
     *   |
     *   |
     *   |
     *   |
     * +-v-+
     * | G |
     * +---+
     */
    private TPhysicalNode mockTestBackStageTPhysicalNode() {

        TPhysicalNode A = new MockTPhysicalNode(new ObjectPath("A", "A"));
        TPhysicalNode B = new MockTPhysicalNode(new ObjectPath("B", "B"));
        TPhysicalNode C = new MockTPhysicalNode(new ObjectPath("C", "C"));
        TPhysicalNode D = new MockTPhysicalNode(new ObjectPath("D", "D"));
        TPhysicalNode E = new MockTPhysicalNode(new ObjectPath("E", "E"));
        TPhysicalNode F = new MockTPhysicalNode(new ObjectPath("F", "F"));
        TPhysicalNode G = new MockTPhysicalNode(new ObjectPath("G", "G"));

        A.addChildNode(B).addChildNode(C);
        B.addChildNode(D).addChildNode(E).addChildNode(F);
        D.addChildNode(G);

        endNode = new MockTPhysicalNode(new ObjectPath(null, null));
        endNode.addParentNode(G).addParentNode(E).addParentNode(F).addParentNode(C);

        ((MockTPhysicalNode) A)
                .addMiddleStageFinishedTable(endNode.getCheckBackStageFinishedTable());
        return A;
    }

    @Test
    void appendTaskToOperator() {
        // create controller operator,mock is do nothing
        MockTPhysicalNode emptyNode = new MockTPhysicalNode(new ObjectPath());
        ProcessorInput input =
                operator.createOperator(emptyNode, new ProcessorInput(), new MockEmptyHandler());
        Function<StageType, AbstractTableTransformerHandler> chooseHandler =
                (type) -> {
                    MockStageTableHandler handler;
                    switch (type) {
                        case FRONT:
                            handler = new MockFrontStageTableHandler();
                            break;
                        case MIDDLE:
                            handler = new MockMiddleStageTableHandler();
                            break;
                        case BACK:
                        default:
                            handler = new MockBackStageTableHandler();
                    }
                    return handler;
                };
        ProcessorInput nextInput = operator.appendTaskOperator(chooseHandler, input);
        operator.createOperator(endNode, nextInput, new MockStageTableHandler(endNode));
    }

    @Test
    void reverseCreated() {
        // create controller operator,mock is do nothing
        MockTPhysicalNode emptyNode = new MockTPhysicalNode(new ObjectPath());
        ProcessorInput input =
                operator.createOperator(emptyNode, new ProcessorInput(), new MockEmptyHandler());
        ProcessorInput processorInput =
                operator.innerFrontCreated(graph, root, new MockFrontStageTableHandler(), input);
        // create root
        ProcessorInput rootInput =
                operator.createOperator(root, processorInput, new MockFrontStageTableHandler(root));
        // check data operator
        operator.createOperator(endNode, rootInput, new MockFrontStageTableHandler(endNode));
    }

    @Test
    void createBackOperator() {
        operator.createOperator(root, new ProcessorInput(), new MockBackStageTableHandler(root));
        ProcessorInput nextInput = operator.createBackOperator(new MockBackStageTableHandler());
        operator.createOperator(endNode, nextInput, new MockBackStageTableHandler(endNode));
    }
}
