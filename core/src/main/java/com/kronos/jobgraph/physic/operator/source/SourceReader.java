package com.kronos.jobgraph.physic.operator.source;

import com.kronos.api.connector.source.SourceEvent;
import com.kronos.api.connector.source.SourceSplit;
import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.api.connector.source.SplitEnumeratorContext;
import com.kronos.api.connector.source.SplitsAssignment;
import com.kronos.runtime.io.InputStatus;
import com.kronos.runtime.io.DataInputStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The interface for a source reader which is responsible for reading the records from the source
 * splits assigned by {@link SplitEnumerator}.
 *
 * @Author: jackila
 * @Date: 10:28 PM 2022-7-25
 */
public interface SourceReader<SplitT extends SourceSplit> extends AutoCloseable{

    void start();

    DataInputStatus pollNext(ReaderOutput out) throws Exception;

    /**
     * Returns a future that signals that data is available from the reader.
     *
     * <p>Once the future completes, the runtime will keep calling the {@link
     * #pollNext(ReaderOutput)} method until that methods returns a status other than {@link
     * InputStatus#MORE_AVAILABLE}. After that the, the runtime will again call this method to
     * obtain the next future. Once that completes, it will again call {@link
     * #pollNext(ReaderOutput)} and so on.
     *
     * <p>The contract is the following: If the reader has data available, then all futures
     * previously returned by this method must eventually complete. Otherwise the source might stall
     * indefinitely.
     *
     * <p>It is not a problem to have occasional "false positives", meaning to complete a future
     * even if no data is available. However, one should not use an "always complete" future in
     * cases no data is available, because that will result in busy waiting loops calling {@code
     * pollNext(...)} even though no data is available.
     *
     * @return a future that will be completed once there is a record available to poll.
     */
    CompletableFuture<Void> isAvailable();

    /**
     * Adds a list of splits for this reader to read. This method is called when the enumerator
     * assigns a split via {@link SplitEnumeratorContext#assignSplit(SourceSplit, int)} or {@link
     * SplitEnumeratorContext#assignSplits(SplitsAssignment)}.
     *
     * @param splits The splits assigned by the split enumerator.
     */
    void addSplits(List<SplitT> splits);

    /**
     * This method is called when the reader is notified that it will not receive any further
     * splits.
     *
     * <p>It is triggered when the enumerator calls {@link
     * SplitEnumeratorContext#signalNoMoreSplits(int)} with the reader's parallel subtask.
     */
    void notifyNoMoreSplits();

    /**
     * Handle a custom source event sent by the {@link SplitEnumerator}. This method is called when
     * the enumerator sends an event via {@link SplitEnumeratorContext#sendEventToSourceReader(int,
     * SourceEvent)}.
     *
     * <p>This method has a default implementation that does nothing, because most sources do not
     * require any custom events.
     *
     * @param sourceEvent the event sent by the {@link SplitEnumerator}.
     */
    default void handleSourceEvents(SourceEvent sourceEvent) {}

}
