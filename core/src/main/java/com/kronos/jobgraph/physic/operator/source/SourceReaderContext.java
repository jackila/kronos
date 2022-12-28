package com.kronos.jobgraph.physic.operator.source;

import com.kronos.api.connector.source.SourceEvent;
import com.kronos.config.Configuration;

/**
 * @Author: jackila
 * @Date: 7:40 PM 2022-8-01
 */
public interface SourceReaderContext {

    /**
     * Sends a split request to the source's {@link SplitEnumerator}. This will result in a call to
     * the {@link SplitEnumerator#handleSplitRequest(int, String)} method, with this reader's
     * parallel subtask id and the hostname where this reader runs.
     *
     */
    void sendSplitRequest();

    /**
     * Send a source event to the source coordinator.
     *
     * @param sourceEvent the source event to coordinator.
     */
    void sendSourceEventToCoordinator(SourceEvent sourceEvent);


    /** @return The index of this subtask. */
    int getIndexOfSubtask();

    Configuration getConfiguration();
}
