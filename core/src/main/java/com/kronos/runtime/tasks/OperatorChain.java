package com.kronos.runtime.tasks;

import com.kronos.api.operators.SourceOperatorFactory;
import com.kronos.api.operators.StreamOperatorFactory;
import com.kronos.api.operators.StreamOperatorParameters;
import com.kronos.jobgraph.physic.operator.StreamOperator;
import com.kronos.runtime.StreamTask;
import com.kronos.runtime.operators.coordination.OperatorCoordinatorHolder;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.operators.coordination.OperatorEventDispatcher;
import com.kronos.runtime.operators.coordination.OperatorEventDispatcherImpl;
import com.kronos.utils.FlinkException;

/**
 * @Author: jackila
 * @Date: 22:59 2022-8-31
 */
public class OperatorChain<OP extends StreamOperator> {

    private final Output streamOutput;

    private final OperatorEventDispatcher operatorEventDispatcher;

    /**
     * this is source operator
     */
    private final StreamOperatorWrapper<OP> mainOperatorWrapper;

    private OperatorCoordinatorHolder coordinatorHolder;

    /**
     * create source operator
     *
     * @param task
     * @param output
     */
    public OperatorChain(StreamTask task,
                         Output output) {
        this.streamOutput = output;

        this.operatorEventDispatcher = createOperatorEventDispacher(task);
        StreamOperatorFactory operatorFactory = new SourceOperatorFactory(task.source());
        if (output != null) {
            StreamOperatorParameters params = new StreamOperatorParameters(operatorEventDispatcher, output);
            mainOperatorWrapper =
                    new StreamOperatorWrapper<>(operatorFactory.createStreamOperator(params));
        } else {
            throw new RuntimeException("no reader setting");
        }
    }

    private OperatorEventDispatcher createOperatorEventDispacher(StreamTask task) {
        return new OperatorEventDispatcherImpl(
                task.environment().getOperatorCoordinatorEventGateway());
    }

    public void dispatchOperatorEvent(int operator, OperatorEvent event)
            throws FlinkException {
        operatorEventDispatcher.dispatchEventToHandlers(operator, event);
    }

    public OP mainOperator() {
        return mainOperatorWrapper.getStreamOperator();
    }
}
