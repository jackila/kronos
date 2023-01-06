package com.kronos.api.operators.source;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.source.ReaderOutput;
import com.kronos.jobgraph.physic.operator.source.SourceOutput;
import com.kronos.runtime.tasks.Output;

/** */
public class StreamingReaderOutput<T> implements ReaderOutput<T> {
    public Output output;

    private final StreamRecord<T> reusingRecord;

    public StreamingReaderOutput(Output output) {

        this.output = output;
        this.reusingRecord = new StreamRecord<>(null);
    }

    @Override
    public void collect(T record) {
        output.collect(reusingRecord.replace(record));
    }

    @Override
    public SourceOutput<T> createOutputForSplit(String splitId) {
        return this;
    }

    @Override
    public void releaseOutputForSplit(String splitId) {}
}
