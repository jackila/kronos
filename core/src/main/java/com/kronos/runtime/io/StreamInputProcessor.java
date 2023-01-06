package com.kronos.runtime.io;

import com.kronos.runtime.tasks.Output;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public final class StreamInputProcessor implements AvailabilityProvider {

    private static final Logger LOG = LoggerFactory.getLogger(StreamInputProcessor.class);

    private StreamTaskInput input;
    private final Output output;

    public StreamInputProcessor(StreamTaskInput input, Output output) {
        this.input = input;
        this.output = output;
    }

    public DataInputStatus processInput() throws Exception {
        DataInputStatus status = input.emitNext(output);
        if (status == DataInputStatus.END_OF_INPUT) {
            // endOfInputAware.endInput(input.getInputIndex() + 1);
        }

        return status;
    }

    @Override
    public CompletableFuture<?> getAvailableFuture() {
        return input.getAvailableFuture();
    }
}
