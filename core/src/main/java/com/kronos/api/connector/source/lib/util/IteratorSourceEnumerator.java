package com.kronos.api.connector.source.lib.util;

import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.api.connector.source.SplitEnumeratorContext;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

/** */
public class IteratorSourceEnumerator implements SplitEnumerator {

    private final SplitEnumeratorContext context;

    private final Queue<IteratorSourceSplit> remainingSplits;

    public IteratorSourceEnumerator(
            SplitEnumeratorContext context, Collection<IteratorSourceSplit> splits) {
        this.context = context;
        this.remainingSplits = new ArrayDeque<>(splits);
    }

    @Override
    public void start() {}

    @Override
    public void handleSplitRequest(int subtaskId) {

        final IteratorSourceSplit nextSplit = remainingSplits.poll();
        if (nextSplit != null) {
            context.assignSplit(nextSplit, subtaskId);
        } else {
            context.signalNoMoreSplits(subtaskId);
        }
    }

    @Override
    public void addReader(int subtaskId) {
        // we don't assign any splits here, because this registration happens after fist startup and
        // after each reader restart/recovery we only want to assign splits once, initially, which
        // we get by reacting to the readers explicit split request
    }

    @Override
    public void close() throws IOException {}
}
