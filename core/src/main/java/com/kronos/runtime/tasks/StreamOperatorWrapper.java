package com.kronos.runtime.tasks;

import com.kronos.jobgraph.physic.operator.StreamOperator;

/** @param <OP> */
public class StreamOperatorWrapper<OP extends StreamOperator> {

    private final OP wrapped;

    public StreamOperatorWrapper(OP wrapped) {
        this.wrapped = wrapped;
    }

    public OP getStreamOperator() {
        return wrapped;
    }
}
