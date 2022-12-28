package com.kronos.api.connector.source.lib.util;

import com.kronos.jobgraph.physic.operator.source.ReaderOutput;
import com.kronos.jobgraph.physic.operator.source.SourceReader;
import com.kronos.jobgraph.physic.operator.source.SourceReaderContext;
import com.kronos.runtime.io.DataInputStatus;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: jackila
 * @Date: 13:21 2022-9-17
 */
public class IteratorSourceReader implements SourceReader<IteratorSourceSplit> {

    /** The context for this reader, to communicate with the enumerator. */
    private final SourceReaderContext context;

    /** The availability future. This reader is available as soon as a split is assigned. */
    private CompletableFuture<Void> availability;

    /**
     * The iterator producing data. Non-null after a split has been assigned. This field is null or
     * non-null always together with the {@link #currentSplit} field.
     */
    private Iterator iterator;

    /**
     * The split whose data we return. Non-null after a split has been assigned. This field is null
     * or non-null always together with the {@link #iterator} field.
     */
    private IteratorSourceSplit currentSplit;

    /** The remaining splits that were assigned but not yet processed. */
    private final Queue<IteratorSourceSplit> remainingSplits;

    private boolean noMoreSplits;

    public IteratorSourceReader(SourceReaderContext context) {
        this.context = context;
        this.availability = new CompletableFuture<>();
        this.remainingSplits = new ArrayDeque<>();
    }


    @Override
    public void start() {
        // request a split if we don't have one
        if (remainingSplits.isEmpty()) {
            context.sendSplitRequest();
        }
    }

    @Override
    public DataInputStatus pollNext(ReaderOutput output) {
        if (iterator != null) {
            if (iterator.hasNext()) {
                output.collect(iterator.next());
                return DataInputStatus.MORE_AVAILABLE;
            } else {
                finishSplit();
            }
        }

        return tryMoveToNextSplit();
    }

    private void finishSplit() {
        iterator = null;
        currentSplit = null;

        // request another split if no other is left
        // we do this only here in the finishSplit part to avoid requesting a split
        // whenever the reader is polled and doesn't currently have a split
        if (remainingSplits.isEmpty() && !noMoreSplits) {
            context.sendSplitRequest();
        }
    }

    private DataInputStatus tryMoveToNextSplit() {
        currentSplit = remainingSplits.poll();
        if (currentSplit != null) {
            iterator = currentSplit.getIterator();
            return DataInputStatus.MORE_AVAILABLE;
        } else if (noMoreSplits) {
            return DataInputStatus.END_OF_INPUT;
        } else {
            // ensure we are not called in a loop by resetting the availability future
            if (availability.isDone()) {
                availability = new CompletableFuture<>();
            }

            return DataInputStatus.NOTHING_AVAILABLE;
        }
    }

    @Override
    public CompletableFuture<Void> isAvailable() {
        return availability;
    }

    @Override
    public void addSplits(List<IteratorSourceSplit> splits) {
        remainingSplits.addAll(splits);
        // set availability so that pollNext is actually called
        availability.complete(null);
    }

    @Override
    public void notifyNoMoreSplits() {
        noMoreSplits = true;
        // set availability so that pollNext is actually called
        availability.complete(null);
    }

    @Override
    public void close() throws Exception {}
}
