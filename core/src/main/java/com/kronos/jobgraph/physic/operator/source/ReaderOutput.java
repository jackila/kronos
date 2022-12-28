package com.kronos.jobgraph.physic.operator.source;

/**
 *
 * it contain more other method other collect
 * such as watermark and status
 * @Author: jackila
 * @Date: 10:18 AM 2022-8-03
 */
public interface ReaderOutput<T> extends SourceOutput<T> {


    /**
     * Creates a {@code SourceOutput} for a specific Source Split. Use these outputs if you want to
     * run split-local logic, like watermark generation.
     *
     * <p>If a split-local output was already created for this split-ID, the method will return that
     * instance, so that only one split-local output exists per split-ID.
     *
     * <p><b>IMPORTANT:</b> After the split has been finished, it is crucial to release the created
     * output again. Otherwise it will continue to contribute to the watermark generation like a
     * perpetually stalling source split, and may hold back the watermark indefinitely.
     *
     * @see #releaseOutputForSplit(String)
     */
    SourceOutput<T> createOutputForSplit(String splitId);

    /**
     * Releases the {@code SourceOutput} created for the split with the given ID.
     *
     * @see #createOutputForSplit(String)
     */
    void releaseOutputForSplit(String splitId);
}
