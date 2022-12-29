package com.kronos.runtime;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.StreamOperator;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.execution.Environment;
import com.kronos.runtime.io.DataInputStatus;
import com.kronos.runtime.io.StreamInputProcessor;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.taskexecutor.TaskExecutorGateway;
import com.kronos.runtime.tasks.OperatorChain;
import com.kronos.runtime.tasks.Output;
import com.kronos.utils.FlinkException;
import com.kronos.utils.NamedThreadFactory;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import static com.kronos.utils.FutureUtils.assertNoException;
import static org.kronos.utils.Preconditions.checkState;

/**
 * @Author: jackila
 * @Date: 11:25 PM 2022-7-22
 */
public abstract class StreamTask<OP extends StreamOperator> implements TaskExecutorGateway {

    private static Logger logger = LoggerFactory.getLogger(StreamTask.class);

    //private Engine engine;

    protected int sourceSize;

    protected ExecutorService executorService;

    protected Output sourceOutput;
    protected StreamInputProcessor streamInputProcessor;

    /**
     * the main operator that consumes the input streams of this task.
     */
    protected OP mainOperator;

    /**
     * Remembers a currently active suspension of the default action. Serves as flag to indicate a
     * suspended default action (suspended if not-null) and to reuse the object as return value in
     * consecutive suspend attempts. Must only be accessed from mailbox thread.
     */
    private volatile DefaultActionSuspension suspendedDefaultAction;

    private Source source;

    private OperatorChain<OP> operatorChain;

    private Environment environment;

    /**
     * Control flag to temporary suspend the mailbox loop/processor. After suspending the mailbox
     * processor can be still later resumed. Must only be accessed from mailbox thread.
     */
    private boolean suspended;

    /**
     * ring buffer init
     */
    public StreamTask(Environment environment) {

        //engine = new Engine();
        sourceSize = 1;
        executorService = Executors.newFixedThreadPool(sourceSize, new NamedThreadFactory());
        this.environment = environment;
        this.source = environment.source();
    }

    /**
     * 环境变量env
     * env中设置各种transformation
     * 调用getStreamGraph
     * 方法内执行translator.translate(transformation)
     * 🌟转换后生成streamGraph
     * 🌟最后执行execute
     * 🌟生成operatorChain
     *
     *
     */

    @SneakyThrows
    public List<Future> execute(JoinPhysicalGraph graph,
                                RingBuffer<StreamRecord<DiffStageRecords>> sourceRingBuffer) {

        // create operator chain

        sourceOutput = new Output(sourceRingBuffer,graph.involvedTarget());
        operatorChain = new OperatorChain(this, sourceOutput);
        // maybe it can be get by a chain struct

        mainOperator = operatorChain.mainOperator();
        //init
        init();
        // -------- Invoke --------
        // many operator can be open by here,but now there is only source operator
        mainOperator.open();

        logger.info("start up source operator");
        List<Future> workStatus = new ArrayList<>();
        for (int i = 0; i < sourceSize; i++) {
            workStatus.add(executorService.submit(this::runLoop));
        }

        logger.info("the operator is end");
        return workStatus;
    }

    @Override
    public void dispatchOperatorEvent(int operator,
                                      OperatorEvent event) throws FlinkException {
        try {
            operatorChain.dispatchOperatorEvent(operator, event);
        } catch (RejectedExecutionException e) {
            // this happens during shutdown, we can swallow this
        }
    }

    protected abstract void init() throws Exception;

    public void runLoop() {
        suspended = false;
        while (isNextLoopPossible()) {
            //初始化source
            processWhenDefaultActionUnavailable();
            if (isNextLoopPossible()) {
                this.processInput();
            }

        }
        logger.info("the task is end.....");
    }

    private void processInput() {
        try {
            DataInputStatus status = streamInputProcessor.processInput();
            switch (status) {
                case MORE_AVAILABLE:
                case NOTHING_AVAILABLE:
                    break;
                case END_OF_RECOVERY:
                    throw new IllegalStateException("We should not receive this event here.");
//                    case END_OF_DATA:
//                        endData();
//                        return;
                case END_OF_INPUT:
                    // Suspend the mailbox processor, it would be resumed in afterInvoke and finished
                    // after all records processed by the downstream tasks. We also suspend the default
                    // actions to avoid repeat executing the empty default operation (namely process
                    // records).
                    suspended = true;
                    return;
            }
            CompletableFuture<?> resumeFuture;
            resumeFuture = streamInputProcessor.getAvailableFuture();
            assertNoException(
                    resumeFuture.thenRun(
                            new ResumeWrapper(suspendDefaultAction())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Suspension suspendDefaultAction() {

        checkState(suspendedDefaultAction == null, "Default action has already been suspended");
        if (suspendedDefaultAction == null) {
            suspendedDefaultAction = new DefaultActionSuspension();
        }
        return suspendedDefaultAction;
    }


    private boolean processWhenDefaultActionUnavailable() {
        boolean processedSomething = false;
        while (!isDefaultActionAvailable() && isNextLoopPossible()) {
            processedSomething = true;
        }
        return processedSomething;
    }

    public boolean isDefaultActionAvailable() {
        return suspendedDefaultAction == null;
    }

    private boolean isNextLoopPossible() {
        // 'Suspended' can be false only when 'mailboxLoopRunning' is true.
        return !suspended;
    }

    // submit stage handler,return next sequence
    class Engine {
        Map<Integer, ExecutorService> runningService = new HashMap<>();
        Map<Integer, EnginWorker> workerPool = new HashMap<>();

        //根据配置初始化
        public Engine() {
        }

        public void addWorker(EnginWorker worker) {
            workerPool.put(worker.operatorId, worker);
        }

        public void start() {
            workerPool.forEach((key, value) -> {
                ExecutorService executor = runningService.getOrDefault(key, Executors.newFixedThreadPool(1,
                                                                                                         new NamedThreadFactory("table-processor-" + value.operatorName)));
                executor.submit(value.worker);
            });

        }
    }

    class EnginWorker {
        EventProcessor worker;
        Integer operatorId;
        String operatorName;

        public EnginWorker(EventProcessor worker,
                           Integer operatorId,
                           String operatorName) {
            this.worker = worker;
            this.operatorId = operatorId;
            this.operatorName = operatorName;
        }
    }

    public static class MessageFactory implements EventFactory<StreamRecord<DiffStageRecords>> {
        @Override
        public StreamRecord newInstance() {
            return new StreamRecord(new DiffStageRecords());
        }
    }

    public Source source() {
        return source;
    }

    public Environment environment() {
        return environment;
    }

    interface Suspension {

        /**
         * Resume execution of the default action.
         */
        void resume();
    }

    /**
     * Represents the suspended state of the default action and offers an idempotent method to
     * resume execution.
     */
    private final class DefaultActionSuspension implements Suspension {

        public DefaultActionSuspension() {
        }

        @Override
        public void resume() {
            resumeInternal();
        }

        private void resumeInternal() {
            if (suspendedDefaultAction == this) {
                suspendedDefaultAction = null;
            }
        }
    }


    private static class ResumeWrapper implements Runnable {
        private final Suspension suspendedDefaultAction;

        public ResumeWrapper(Suspension suspendedDefaultAction) {
            this.suspendedDefaultAction = suspendedDefaultAction;
        }

        @Override
        public void run() {
            suspendedDefaultAction.resume();
        }
    }

}
