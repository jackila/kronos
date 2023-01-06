package com.kronos.jobgraph.physic.operator;

import com.kronos.jobgraph.physic.StreamRecord;
import com.lmax.disruptor.RingBuffer;

/** */
public class StreamTaskContext<T> {
    private RingBuffer<StreamRecord<T>> ringBuffer;

    public void emitRecord(T record) {
        long sequece = ringBuffer.next();
        StreamRecord streamRecord = ringBuffer.get(sequece);
        streamRecord.setValue(record);
        ringBuffer.publish(sequece);
    }
}
