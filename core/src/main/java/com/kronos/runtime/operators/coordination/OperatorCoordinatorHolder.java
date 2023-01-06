/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.runtime.operators.coordination;

import static org.kronos.utils.Preconditions.checkNotNull;
import static org.kronos.utils.Preconditions.checkState;

import com.kronos.runtime.executiongraph.ExecutionJobVertex;
import com.kronos.runtime.source.coordinator.OperatorCoordinator;
import com.kronos.runtime.source.coordinator.SubtaskGatewayImpl;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.kronos.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code OperatorCoordinatorHolder} holds the {@link OperatorCoordinator} and manages all its
 * interactions with the remaining components. It provides the context and is responsible for
 * checkpointing and exactly once semantics.
 *
 * <h3>Exactly-one Semantics</h3>
 *
 * <p>The semantics are described under {@link OperatorCoordinator#checkpointCoordinator(long,
 * CompletableFuture)}.
 *
 * <h3>Exactly-one Mechanism</h3>
 *
 * <p>This implementation can handle one checkpoint being triggered at a time. If another checkpoint
 * is triggered while the triggering of the first one was not completed or aborted, this class will
 * throw an exception. That is in line with the capabilities of the Checkpoint Coordinator, which
 * can handle multiple concurrent checkpoints on the TaskManagers, but only one concurrent
 * triggering phase.
 *
 * <p>The mechanism for exactly once semantics is as follows:
 *
 * <ul>
 *   <li>Events pass through a special channel, the {@link OperatorEventValve}. If we are not
 *       currently triggering a checkpoint, then events simply pass through.
 *   <li>With the completion of the checkpoint future for the coordinator, this operator event valve
 *       is closed. Events coming after that are held back (buffered), because they belong to the
 *       epoch after the checkpoint.
 *   <li>Once all coordinators in the job have completed the checkpoint, the barriers to the sources
 *       are injected. After that (see ) the valves are opened again and the events are sent.
 *   <li>If a task fails in the meantime, the events are dropped from the valve. From the
 *       coordinator's perspective, these events are lost, because they were sent to a failed
 *       subtask after it's latest complete checkpoint.
 * </ul>
 *
 * <p><b>IMPORTANT:</b> A critical assumption is that all events from the scheduler to the Tasks are
 * transported strictly in order. Events being sent from the coordinator after the checkpoint
 * barrier was injected must not overtake the checkpoint barrier. This is currently guaranteed by
 * Flink's RPC mechanism.
 *
 * <p>Consider this example:
 *
 * <pre>
 * Coordinator one events: => a . . b . |trigger| . . |complete| . . c . . d . |barrier| . e . f
 * Coordinator two events: => . . x . . |trigger| . . . . . . . . . .|complete||barrier| . . y . . z
 * </pre>
 *
 * <p>Two coordinators trigger checkpoints at the same time. 'Coordinator Two' takes longer to
 * complete, and in the meantime 'Coordinator One' sends more events.
 *
 * <p>'Coordinator One' emits events 'c' and 'd' after it finished its checkpoint, meaning the
 * events must take place after the checkpoint. But they are before the barrier injection, meaning
 * the runtime task would see them before the checkpoint, if they were immediately transported.
 *
 * <p>'Coordinator One' closes its valve as soon as the checkpoint future completes. Events 'c' and
 * 'd' get held back in the valve. Once 'Coordinator Two' completes its checkpoint, the barriers are
 * sent to the sources. Then the valves are opened, and events 'c' and 'd' can flow to the tasks
 * where they are received after the barrier.
 *
 * <h3>Concurrency and Threading Model</h3>
 *
 * <p>This component runs strictly in the Scheduler's main-thread-executor. All calls "from the
 * outside" are either already in the main-thread-executor (when coming from Scheduler) or put into
 * the main-thread-executor (when coming from the CheckpointCoordinator). We rely on the executor to
 * preserve strict order of the calls.
 *
 * <p>Actions from the coordinator to the "outside world" (like completing a checkpoint and sending
 * an event) are also enqueued back into the scheduler main-thread executor, strictly in order.
 */
public class OperatorCoordinatorHolder implements OperatorInfo, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OperatorCoordinatorHolder.class);

    private final OperatorCoordinator coordinator;
    private final int operatorId;
    private final LazyInitializedCoordinatorContext context;
    private final SubtaskAccess.SubtaskAccessFactory taskAccesses;
    private final int operatorParallelism;
    private final int operatorMaxParallelism;

    private Consumer<Throwable> globalFailureHandler;

    private OperatorCoordinatorHolder(
            final int operatorId,
            final OperatorCoordinator coordinator,
            final LazyInitializedCoordinatorContext context,
            final SubtaskAccess.SubtaskAccessFactory taskAccesses,
            final int operatorParallelism,
            final int operatorMaxParallelism) {

        this.operatorId = checkNotNull(operatorId);
        this.coordinator = checkNotNull(coordinator);
        this.context = checkNotNull(context);
        this.taskAccesses = checkNotNull(taskAccesses);
        this.operatorParallelism = operatorParallelism;
        this.operatorMaxParallelism = operatorMaxParallelism;
    }

    public void lazyInitialize() {

        context.lazyInitialize();

        setupAllSubtaskGateways();
    }

    // ------------------------------------------------------------------------
    //  Properties
    // ------------------------------------------------------------------------

    public OperatorCoordinator coordinator() {
        return coordinator;
    }

    @Override
    public int operatorId() {
        return operatorId;
    }

    @Override
    public int maxParallelism() {
        return operatorMaxParallelism;
    }

    @Override
    public int currentParallelism() {
        return operatorParallelism;
    }

    // ------------------------------------------------------------------------
    //  OperatorCoordinator Interface
    // ------------------------------------------------------------------------

    public void start() throws Exception {
        coordinator.start();
    }

    public void close() throws Exception {
        coordinator.close();
        context.unInitialize();
    }

    public void handleEventFromOperator(int subtask, OperatorEvent event) throws Exception {
        coordinator.handleEventFromOperator(subtask, event);
    }

    // ------------------------------------------------------------------------
    //  miscellaneous helpers
    // ------------------------------------------------------------------------

    private void setupAllSubtaskGateways() {
        for (int i = 0; i < operatorParallelism; i++) {
            setupSubtaskGateway(i);
        }
    }

    private void setupSubtaskGateway(int subtask) {
        // this gets an access to the latest task execution attempt.
        final SubtaskAccess sta = taskAccesses.getAccessForSubtask(subtask);

        final OperatorCoordinator.SubtaskGateway gateway = new SubtaskGatewayImpl(sta);

        notifySubtaskReady(subtask, gateway);
    }

    private void notifySubtaskReady(int subtask, OperatorCoordinator.SubtaskGateway gateway) {
        try {
            coordinator.subtaskReady(subtask, gateway);
        } catch (Throwable t) {
            ExceptionUtils.rethrowIfFatalErrorOrOOM(t);
            globalFailureHandler.accept(new RuntimeException("Error from OperatorCoordinator", t));
        }
    }

    // ------------------------------------------------------------------------
    //  Factories
    // ------------------------------------------------------------------------

    public static OperatorCoordinatorHolder create(
            OperatorCoordinator.Provider provider, ExecutionJobVertex jobVertex) throws Exception {

        final int opId = provider.getOperatorId();

        final SubtaskAccess.SubtaskAccessFactory taskAccesses =
                new ExecutionSubtaskAccess.ExecutionJobVertexSubtaskAccess(jobVertex, opId);

        return create(
                opId,
                provider,
                jobVertex.getName(),
                jobVertex.getParallelism(),
                jobVertex.getMaxParallelism(),
                taskAccesses);
    }

    static OperatorCoordinatorHolder create(
            final int opId,
            final OperatorCoordinator.Provider coordinatorProvider,
            final String operatorName,
            final int operatorParallelism,
            final int operatorMaxParallelism,
            final SubtaskAccess.SubtaskAccessFactory taskAccesses)
            throws Exception {

        final LazyInitializedCoordinatorContext context =
                new LazyInitializedCoordinatorContext(opId, operatorName, operatorParallelism);

        final OperatorCoordinator coordinator = coordinatorProvider.create(context);

        return new OperatorCoordinatorHolder(
                opId,
                coordinator,
                context,
                taskAccesses,
                operatorParallelism,
                operatorMaxParallelism);
    }

    // ------------------------------------------------------------------------
    //  Nested Classes
    // ------------------------------------------------------------------------

    /**
     * An implementation of the {@link OperatorCoordinator.Context}.
     *
     * <p>All methods are safe to be called from other threads than the Scheduler's and the
     * JobMaster's main threads.
     *
     * <p>Implementation note: Ideally, we would like to operate purely against the scheduler
     * interface, but it is not exposing enough information at the moment.
     */
    private static final class LazyInitializedCoordinatorContext
            implements OperatorCoordinator.Context {

        private static final Logger LOG =
                LoggerFactory.getLogger(LazyInitializedCoordinatorContext.class);

        private final int operatorId;
        private final String operatorName;
        private final int operatorParallelism;

        private Executor schedulerExecutor;

        private volatile boolean failed;

        public LazyInitializedCoordinatorContext(
                final int operatorId, final String operatorName, final int operatorParallelism) {
            this.operatorId = checkNotNull(operatorId);
            this.operatorName = checkNotNull(operatorName);
            this.operatorParallelism = operatorParallelism;
        }

        void lazyInitialize() {
            // check executor
        }

        void unInitialize() {
            this.schedulerExecutor = null;
        }

        boolean isInitialized() {
            return schedulerExecutor != null;
        }

        private void checkInitialized() {
            checkState(isInitialized(), "Context was not yet initialized");
        }

        void resetFailed() {
            failed = false;
        }

        public int getOperatorId() {
            return operatorId;
        }

        @Override
        public int currentParallelism() {
            return operatorParallelism;
        }
    }
}
