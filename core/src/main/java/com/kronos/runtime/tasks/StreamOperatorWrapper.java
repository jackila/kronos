package com.kronos.runtime.tasks;

import com.kronos.jobgraph.physic.operator.StreamOperator;

/**
 * @Author: jackila
 * @Date: 23:48 2022-8-31
 */
public class StreamOperatorWrapper <OP extends StreamOperator> {

    private final OP wrapped;

    public StreamOperatorWrapper(OP wrapped) {
        this.wrapped = wrapped;
    }

    public OP getStreamOperator() {
        return wrapped;
    }
}
