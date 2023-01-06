package com.kronos.api.connector.source;

import java.io.IOException;

/** */
public interface SplitEnumerator<SplitT extends SourceSplit> {
    /**
     * Start the split enumerator.
     *
     * <p>The default behavior does nothing.
     */
    void start();

    /**
     * Handles the request for a split. This method is called when the reader with the given subtask
     * id calls the {@link SourceReaderContext#sendSplitRequest()} method.
     *
     * @param subtaskId the subtask id of the source reader who sent the source event.
     */
    void handleSplitRequest(int subtaskId);

    /**
     * Add a new source reader with the given subtask ID.
     *
     * @param subtaskId the subtask ID of the new source reader.
     */
    void addReader(int subtaskId);

    /**
     * Called to close the enumerator, in case it holds on to any resources, like threads or network
     * connections.
     */
    void close() throws IOException;

    /**
     * Handles a custom source event from the source reader.
     *
     * <p>This method has a default implementation that does nothing, because it is only required to
     * be implemented by some sources, which have a custom event protocol between reader and
     * enumerator. The common events for reader registration and split requests are not dispatched
     * to this method, but rather invoke the {@link #addReader(int)} and {@link
     * #handleSplitRequest(int, String)} methods.
     *
     * @param subtaskId the subtask id of the source reader who sent the source event.
     * @param sourceEvent the source event from the source reader.
     */
    default void handleSourceEvent(int subtaskId, SourceEvent sourceEvent) {}
}
