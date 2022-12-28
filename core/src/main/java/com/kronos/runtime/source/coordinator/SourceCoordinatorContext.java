package com.kronos.runtime.source.coordinator;

import com.kronos.api.connector.source.ReaderInfo;
import com.kronos.api.connector.source.SourceEvent;
import com.kronos.api.connector.source.SourceSplit;
import com.kronos.api.connector.source.SplitEnumeratorContext;
import com.kronos.api.connector.source.SplitsAssignment;
import com.kronos.runtime.source.even.AddSplitEvent;
import com.kronos.runtime.source.even.NoMoreSplitsEvent;
import com.kronos.runtime.source.even.SourceEventWrapper;
import com.kronos.utils.ExecutorThreadFactory;
import com.kronos.utils.FlinkRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @Author: jackila
 * @Date: 11:45 2022-10-15
 */

/**
 * A context class for the {@link OperatorCoordinator}. Compared with {@link SplitEnumeratorContext}
 * this class allows interaction with state and sending {@link OperatorEvent} to the SourceOperator
 * while {@link SplitEnumeratorContext} only allows sending {@link SourceEvent}.
 *
 * <p>The context serves a few purposes:
 *
 * <ul>
 *   <li>Information provider - The context provides necessary information to the enumerator for it
 *       to know what is the status of the source readers and their split assignments. These
 *       information allows the split enumerator to do the coordination.
 *   <li>Action taker - The context also provides a few actions that the enumerator can take to
 *       carry out the coordination. So far there are two actions: 1) assign splits to the source
 *       readers. and 2) sens a custom {@link SourceEvent SourceEvents} to the source readers.
 *   <li>Thread model enforcement - The context ensures that all the manipulations to the
 *       coordinator state are handled by the same thread.
 * </ul>
 *
 * @param <SplitT> the type of the splits.
 */
public class SourceCoordinatorContext<SplitT extends SourceSplit>
        implements SplitEnumeratorContext<SplitT> {
    private static final Logger LOG = LoggerFactory.getLogger(SourceCoordinatorContext.class);

    private final ExecutorService coordinatorExecutor;

    private final OperatorCoordinator.Context operatorCoordinatorContext;
    private final ConcurrentMap<Integer, ReaderInfo> registeredReaders;
    private final SplitAssignmentTracker<SplitT> assignmentTracker;
    private final SourceCoordinatorProvider.CoordinatorExecutorThreadFactory
            coordinatorThreadFactory;
    private final OperatorCoordinator.SubtaskGateway[] subtaskGateways;
    private final String coordinatorThreadName;
    private volatile boolean closed;

    public SourceCoordinatorContext(
            ExecutorService coordinatorExecutor,
            SourceCoordinatorProvider.CoordinatorExecutorThreadFactory coordinatorThreadFactory,
            int numWorkerThreads,
            OperatorCoordinator.Context operatorCoordinatorContext
    ) {
        this(
                coordinatorExecutor,
                Executors.newScheduledThreadPool(
                        numWorkerThreads,
                        new ExecutorThreadFactory(
                                coordinatorThreadFactory.getCoordinatorThreadName() + "-worker")),
                coordinatorThreadFactory,
                operatorCoordinatorContext,
                new SplitAssignmentTracker<>());
    }

    // Package private method for unit test.
    SourceCoordinatorContext(
            ExecutorService coordinatorExecutor,
            ScheduledExecutorService workerExecutor,
            SourceCoordinatorProvider.CoordinatorExecutorThreadFactory coordinatorThreadFactory,
            OperatorCoordinator.Context operatorCoordinatorContext,
            SplitAssignmentTracker<SplitT> splitAssignmentTracker) {
        this.coordinatorExecutor = coordinatorExecutor;
        this.coordinatorThreadFactory = coordinatorThreadFactory;
        this.operatorCoordinatorContext = operatorCoordinatorContext;
        this.registeredReaders = new ConcurrentHashMap<>();
        this.assignmentTracker = splitAssignmentTracker;
        this.coordinatorThreadName = coordinatorThreadFactory.getCoordinatorThreadName();
        this.subtaskGateways =
                new OperatorCoordinator.SubtaskGateway
                        [operatorCoordinatorContext.currentParallelism()];

    }

    @Override
    public void sendEventToSourceReader(int subtaskId,
                                        SourceEvent event) {
        callInCoordinatorThread(
                () -> {
                    final OperatorCoordinator.SubtaskGateway gateway =
                            getGatewayAndCheckReady(subtaskId);
                    gateway.sendEvent(new SourceEventWrapper(event));
                    return null;
                },
                String.format("Failed to send event %s to subtask %d", event, subtaskId));

    }

    @Override
    public int currentParallelism() {
        return operatorCoordinatorContext.currentParallelism();
    }

    @Override
    public Map<Integer, ReaderInfo> registeredReaders() {
        return Collections.unmodifiableMap(registeredReaders);
    }

    @Override
    public void assignSplits(SplitsAssignment<SplitT> assignment) {

        // Ensure all the subtasks in the assignment have registered.

        assignmentTracker.recordSplitAssignment(assignment);
        assignment
                .assignment()
                .forEach(
                        (id, splits) -> {
                            final OperatorCoordinator.SubtaskGateway gateway =
                                    getGatewayAndCheckReady(id);

                            final AddSplitEvent addSplitEvent;
                            try {
                                addSplitEvent =
                                        new AddSplitEvent(splits);
                                gateway.sendEvent(addSplitEvent);
                            } catch (Exception e) {
                                throw new FlinkRuntimeException(
                                        "Failed to serialize splits.", e);
                            }

                        });
    }

    @Override
    public void signalNoMoreSplits(int subtask) {
        final OperatorCoordinator.SubtaskGateway gateway =
                getGatewayAndCheckReady(subtask);
        try {
            gateway.sendEvent(new NoMoreSplitsEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void callAsync(Callable<T> callable,
                              BiConsumer<T, Throwable> handler) {

    }

    @Override
    public <T> void callAsync(Callable<T> callable,
                              BiConsumer<T, Throwable> handler,
                              long initialDelay,
                              long period) {

    }

    public void close() throws InterruptedException {
        closed = true;
        coordinatorExecutor.shutdown();
        coordinatorExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runInCoordinatorThread(Runnable runnable) {
        coordinatorExecutor.execute(runnable);
    }

    OperatorCoordinator.SubtaskGateway getGatewayAndCheckReady(int subtaskIndex) {
        final OperatorCoordinator.SubtaskGateway gateway = subtaskGateways[subtaskIndex];
        if (gateway != null) {
            return gateway;
        }

        throw new IllegalStateException(
                String.format("Subtask %d is not ready yet to receive events.", subtaskIndex));
    }

    /**
     * A helper method that delegates the callable to the coordinator thread if the current thread
     * is not the coordinator thread, otherwise call the callable right away.
     *
     * @param callable the callable to delegate.
     */
    private <V> V callInCoordinatorThread(Callable<V> callable,
                                          String errorMessage) {
        // Ensure the split assignment is done by the the coordinator executor.
        if (!coordinatorThreadFactory.isCurrentThreadCoordinatorThread()) {
            try {
                return coordinatorExecutor.submit(callable).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(errorMessage, e);
            }
        }

        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }


    /**
     * Register a source reader.
     *
     * @param readerInfo the reader information of the source reader.
     */
    void registerSourceReader(ReaderInfo readerInfo) {
        final ReaderInfo previousReader =
                registeredReaders.put(readerInfo.subtaskId(), readerInfo);
        if (previousReader != null) {
            throw new IllegalStateException(
                    "Overwriting " + previousReader + " with " + readerInfo);
        }
    }

    /**
     * Unregister a source reader.
     *
     * @param subtaskId the subtask id of the source reader.
     */
    void unregisterSourceReader(int subtaskId) {
        registeredReaders.remove(subtaskId);
    }

    public OperatorCoordinator.Context getCoordinatorContext() {
        return operatorCoordinatorContext;
    }

    public void subtaskReady(OperatorCoordinator.SubtaskGateway gateway) {
        final int subtask = gateway.getSubtask();
        if (subtaskGateways[subtask] == null) {
            subtaskGateways[gateway.getSubtask()] = gateway;
        } else {
            throw new IllegalStateException("Already have a subtask gateway for " + subtask);
        }
    }
}
