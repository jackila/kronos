package com.kronos.runtime.io;

import com.kronos.jobgraph.physic.operator.source.SourceOperator;
import com.kronos.runtime.tasks.Output;
import java.util.concurrent.CompletableFuture;

/** 作为一个processor 的input，it cantain a operator. */
public class StreamTaskSourceInput implements StreamTaskInput {
    private final SourceOperator operator;
    private final int inputIndex;

    private final AvailabilityHelper isBlockedAvailability = new AvailabilityHelper();

    public StreamTaskSourceInput(SourceOperator operator, int inputIndex) {
        this.operator = operator;
        this.inputIndex = inputIndex;
        isBlockedAvailability.resetAvailable();
    }

    @Override
    public int getInputIndex() {
        return this.inputIndex;
    }

    /**
     * 需要处理一些状态信息，比如是否operator available and return the status of Nothing_available
     *
     * @param output
     * @throws Exception
     */
    @Override
    public DataInputStatus emitNext(Output output) throws Exception {
        /**
         * Safe guard against best efforts availability checks. If despite being unavailable someone
         * polls the data from this source while it's blocked, it should return {@link
         * DataInputStatus.NOTHING_AVAILABLE}.
         */
        if (isBlockedAvailability.isApproximatelyAvailable()) {
            return operator.emitNext(output);
        }
        return DataInputStatus.NOTHING_AVAILABLE;
    }

    @Override
    public CompletableFuture<?> getAvailableFuture() {
        return isBlockedAvailability.and(operator);
    }
}
