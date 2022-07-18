package com.kronos.core.operators;

/**
 * 创建 StreamOperator的工厂方法
 * @Author: jackila
 * @Date: 2:47 PM 2022-6-18
 */
public interface StreamOperatorFactory<OUT> {
    /** Create the operator. Sets access to the context and the output. */
    <T extends StreamOperator<OUT>> T createStreamOperator(
            StreamOperatorParameters<OUT> parameters);

    /** Is this factory for StreamSource */
    default boolean isStreamSource() {
        return false;
    }

    default boolean isLegacySource() {
        return false;
    }

}
