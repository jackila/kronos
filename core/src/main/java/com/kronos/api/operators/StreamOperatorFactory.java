package com.kronos.api.operators;

import com.kronos.jobgraph.physic.operator.StreamOperator;

/**
 * @Author: jackila
 * @Date: 23:12 2022-8-31
 */
public interface StreamOperatorFactory {

    public abstract  <T extends StreamOperator> T createStreamOperator(
            StreamOperatorParameters parameters);
}
