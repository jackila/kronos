package com.kronos.jobgraph.physic.operator.source;

/**
 * a gateway for source reader. compare ReaderOutput it just has basic method collect PM 2022-8-01
 */
public interface SourceOutput<T> {
    void collect(T record);
}
