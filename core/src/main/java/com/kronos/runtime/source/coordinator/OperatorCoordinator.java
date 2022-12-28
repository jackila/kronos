package com.kronos.runtime.source.coordinator;

import com.kronos.runtime.message.Acknowledge;
import com.kronos.runtime.operators.coordination.OperatorEvent;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * Operator coordinators are for example source and sink coordinators that discover and assign
 * work, or aggregate and commit metadata.
 * @Author: jackila
 * @Date: 01:07 2022-10-15
 */
public interface OperatorCoordinator {
    /**
     * Starts the coordinator. This method is called once at the beginning, before any other
     * methods.
     *
     * @throws Exception Any exception thrown from this method causes a full job failure.
     */
    void start() throws Exception;

    /**
     * This method is called when the coordinator is disposed. This method should release currently
     * held resources. Exceptions in this method do not cause the job to fail.
     */
    void close() throws Exception;

    // ------------------------------------------------------------------------

    /**
     * Hands an OperatorEvent coming from a parallel Operator instances (one of the parallel
     * subtasks).
     *
     * @throws Exception Any exception thrown by this method results in a full job failure and
     *     recovery.
     */
    void handleEventFromOperator(int subtask, OperatorEvent event) throws Exception;

    /**
     * This is called when a subtask of the Operator becomes ready to receive events, both after
     * initial startup and after task failover. The given {@code SubtaskGateway} can be used to send
     * events to the executed subtask.
     *
     * <p>The given {@code SubtaskGateway} is bound to that specific execution attempt that became
     * ready. All events sent through the gateway target that execution attempt; if the attempt is
     * no longer running by the time the event is sent, then the events are failed.
     */
    void subtaskReady(int subtask, SubtaskGateway gateway);

    /**
     * The context gives the OperatorCoordinator access to contextual information and provides a
     * gateway to interact with other components, such as sending operator events.
     */
    interface Context {
        /** Gets the current parallelism with which this operator is executed. */
        int currentParallelism();


        int getOperatorId();
    }
    // ------------------------------------------------------------------------

    /**
     * The {@code SubtaskGateway} is the way to interact with a specific parallel instance of the
     * Operator (an Operator subtask), like sending events to the operator.
     */
    interface SubtaskGateway {

        /**
         * Sends an event to the parallel subtask with the given subtask index.
         *
         * <p>The returned future is completed successfully once the event has been received by the
         * target TaskManager. The future is completed exceptionally if the event cannot be sent.
         * That includes situations where the target task is not running.
         *
         * @return
         */
        void sendEvent(OperatorEvent evt) throws Exception;

        /**
         * Gets the subtask index of the parallel operator instance this gateway communicates with.
         */
        int getSubtask();
    }

    // ------------------------------------------------------------------------

    /**
     * The provider creates an OperatorCoordinator and takes a {@link Context} to pass to the
     * OperatorCoordinator. This method is, for example, called on the job manager when the
     * scheduler and execution graph are created, to instantiate the OperatorCoordinator.
     *
     * <p>The factory is {@link Serializable}, because it is attached to the JobGraph and is part of
     * the serialized job graph that is sent to the dispatcher, or stored for recovery.
     */
    interface Provider extends Serializable {

        /** Gets the ID of the operator to which the coordinator belongs. */
        int getOperatorId();

        /** Creates the {@code OperatorCoordinator}, using the given context. */
        OperatorCoordinator create(Context context) throws Exception;
    }
}
