package com.kronos.jobgraph.physic.operator.source;

import com.kronos.api.connector.source.SourceEvent;
import com.kronos.api.connector.source.SourceSplit;
import com.kronos.api.connector.source.lib.util.IteratorSourceSplit;
import com.kronos.api.operators.source.StreamingReaderOutput;
import com.kronos.config.Configuration;
import com.kronos.jobgraph.physic.AbstractStreamOperator;
import com.kronos.runtime.io.AvailabilityProvider;
import com.kronos.runtime.io.DataInputStatus;
import com.kronos.runtime.io.MultipleFuturesAvailabilityHelper;
import com.kronos.runtime.io.PushingAsyncDataInput;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.operators.coordination.OperatorEventGateway;
import com.kronos.runtime.operators.coordination.OperatorEventHandler;
import com.kronos.runtime.source.even.AddSplitEvent;
import com.kronos.runtime.source.even.NoMoreSplitsEvent;
import com.kronos.runtime.source.even.ReaderRegistrationEvent;
import com.kronos.runtime.source.even.RequestSplitEvent;
import com.kronos.runtime.source.even.SourceEventWrapper;
import com.kronos.runtime.tasks.Output;
import com.kronos.utils.CollectionUtil;
import com.kronos.utils.FunctionWithException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * before flink use setup to init reader , now they select use readerFactory to achieve the target
 * 2022-7-25
 */
public class SourceOperator<OUT, SplitT extends SourceSplit> extends AbstractStreamOperator
        implements PushingAsyncDataInput, OperatorEventHandler {
    /** The source reader that does most of the work. */
    private SourceReader sourceReader;

    private ReaderOutput currentMainOutput;

    /** The state that holds the currently assigned splits. */
    private List<IteratorSourceSplit> readerState;

    /** A mode to control the behaviour of the {@link #emitNext(DataOutput)} method. */
    private OperatingMode operatingMode;

    private final SourceOperatorAvailabilityHelper availabilityHelper =
            new SourceOperatorAvailabilityHelper();

    /**
     * The factory for the source reader. This is a workaround, because currently the SourceReader
     * must be lazily initialized, which is mainly because the metrics groups that the reader relies
     * on is lazily initialized.
     */
    private final FunctionWithException<SourceReaderContext, SourceReader, Exception> readerFactory;

    /** The event gateway through which this operator talks to its coordinator. */
    private final OperatorEventGateway operatorEventGateway;

    private final CompletableFuture<Void> finished = new CompletableFuture<>();
    private int index = 0;

    private enum OperatingMode {
        READING,
        OUTPUT_NOT_INITIALIZED,
        SOURCE_STOPPED,
        DATA_FINISHED
    }

    /**
     * todo construct coordinator\config
     *
     * @param readerFactory
     */
    public SourceOperator(
            FunctionWithException readerFactory, OperatorEventGateway operatorEventGateway) {
        this.readerFactory = readerFactory;
        this.operatorEventGateway = operatorEventGateway;
        this.operatingMode = OperatingMode.OUTPUT_NOT_INITIALIZED;
    }

    public void initReader() throws Exception {

        if (sourceReader != null) {
            return;
        }
        // 作为与enumer split之间的沟通渠道
        final SourceReaderContext context =
                new SourceReaderContext() {

                    @Override
                    public void sendSplitRequest() {
                        operatorEventGateway.sendEventToCoordinator(new RequestSplitEvent());
                    }

                    @Override
                    public void sendSourceEventToCoordinator(SourceEvent event) {
                        operatorEventGateway.sendEventToCoordinator(new SourceEventWrapper(event));
                    }

                    @Override
                    public int getIndexOfSubtask() {
                        return 0;
                    }

                    @Override
                    public Configuration getConfiguration() {
                        return null;
                    }
                };

        sourceReader = readerFactory.apply(context);
    }

    @Override
    public void open() throws Exception {

        initReader();
        // restore the state if necessary.
        final List<IteratorSourceSplit> splits = CollectionUtil.iterableToList(readerState);
        if (!splits.isEmpty()) {
            sourceReader.addSplits(splits);
        }

        // Register the reader to the coordinator.
        registerReader();

        // Start the reader after registration, sending messages in start is allowed.
        sourceReader.start();
    }

    private void registerReader() {
        operatorEventGateway.sendEventToCoordinator(new ReaderRegistrationEvent(index));
    }

    public CompletableFuture<Void> stop() {
        switch (operatingMode) {
            case OUTPUT_NOT_INITIALIZED:
            case READING:
                this.operatingMode = OperatingMode.SOURCE_STOPPED;
                availabilityHelper.forceStop();
                break;
        }
        return finished;
    }

    @Override
    public void close() throws Exception {
        if (sourceReader != null) {
            sourceReader.close();
        }
    }

    @Override
    public DataInputStatus emitNext(Output output) throws Exception {
        // short circuit the hot path. Without this short circuit (READING handled in the
        // switch/case) InputBenchmark.mapSink was showing a performance regression.
        if (operatingMode == OperatingMode.READING) {
            return convertToInternalStatus(sourceReader.pollNext(currentMainOutput));
        }
        if (currentMainOutput != null) {
            sourceReader.pollNext(currentMainOutput);
        }
        // init current main output
        currentMainOutput = new StreamingReaderOutput(output);
        return sourceReader.pollNext(currentMainOutput);
    }

    private DataInputStatus convertToInternalStatus(DataInputStatus inputStatus) {
        switch (inputStatus) {
            case MORE_AVAILABLE:
                return DataInputStatus.MORE_AVAILABLE;
            case NOTHING_AVAILABLE:
                return DataInputStatus.NOTHING_AVAILABLE;
            case END_OF_INPUT:
                this.operatingMode = OperatingMode.DATA_FINISHED;
                return DataInputStatus.END_OF_DATA;
            default:
                throw new IllegalArgumentException("Unknown input status: " + inputStatus);
        }
    }

    @Override
    public void handleOperatorEvent(OperatorEvent event) {
        if (event instanceof AddSplitEvent) {
            sourceReader.addSplits(((AddSplitEvent) event).splits());
        } else if (event instanceof SourceEventWrapper) {
            sourceReader.handleSourceEvents(((SourceEventWrapper) event).getSourceEvent());
        } else if (event instanceof NoMoreSplitsEvent) {
            sourceReader.notifyNoMoreSplits();
        } else {
            throw new IllegalStateException("Received unexpected operator event " + event);
        }
    }

    @Override
    public CompletableFuture<?> getAvailableFuture() {
        switch (operatingMode) {
            case OUTPUT_NOT_INITIALIZED:
            case READING:
                return availabilityHelper.update(sourceReader.isAvailable());
            case SOURCE_STOPPED:
            case DATA_FINISHED:
                return AvailabilityProvider.AVAILABLE;
            default:
                throw new IllegalStateException("Unknown operating mode: " + operatingMode);
        }
    }

    private static class SourceOperatorAvailabilityHelper {
        private final CompletableFuture<Void> forcedStopFuture = new CompletableFuture<>();
        private final MultipleFuturesAvailabilityHelper availabilityHelper;

        private SourceOperatorAvailabilityHelper() {
            availabilityHelper = new MultipleFuturesAvailabilityHelper(2);
            availabilityHelper.anyOf(0, forcedStopFuture);
        }

        public CompletableFuture<?> update(CompletableFuture<Void> sourceReaderFuture) {
            if (sourceReaderFuture == AvailabilityProvider.AVAILABLE
                    || sourceReaderFuture.isDone()) {
                return AvailabilityProvider.AVAILABLE;
            }
            availabilityHelper.resetToUnAvailable();
            availabilityHelper.anyOf(0, forcedStopFuture);
            availabilityHelper.anyOf(1, sourceReaderFuture);
            return availabilityHelper.getAvailableFuture();
        }

        public void forceStop() {
            forcedStopFuture.complete(null);
        }
    }
}
