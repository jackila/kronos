package com.kronos.runtime.executiongraph;

import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.utils.FlinkException;

/** Task Manager -----> Operator (Task). */
public interface TaskExecutorOperatorEventGateway {
    /**
     * Sends an operator event to an operator in a task executed by the Task Manager (Task
     * Executor).
     *
     * <p>The reception is acknowledged (future is completed) when the event has been dispatched to
     * the {@link
     * org.apache.flink.runtime.jobgraph.tasks.AbstractInvokable#dispatchOperatorEvent(OperatorID,
     * SerializedValue)} method. It is not guaranteed that the event is processed successfully
     * within the implementation. These cases are up to the task and event sender to handle (for
     * example with an explicit response message upon success, or by triggering failure/recovery
     * upon exception).
     */
    void dispatchOperatorEvent(int operator, OperatorEvent event) throws FlinkException;
}
