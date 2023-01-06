package com.kronos.api.operators;

import com.kronos.jobgraph.physic.operator.StreamOperator;

/** */
public interface StreamOperatorFactory {

    public abstract <T extends StreamOperator> T createStreamOperator(
            StreamOperatorParameters parameters);
}
