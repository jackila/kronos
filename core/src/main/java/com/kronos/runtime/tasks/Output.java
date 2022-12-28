package com.kronos.runtime.tasks;

import com.kronos.api.operators.Collector;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.table.ObjectPath;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * 算子之间的数据传输
 * source --- > operator
 *
 * @Author: jackila
 * @Date: 8:58 AM 2022-8-11
 */
@Slf4j
public class Output implements Collector<StreamRecord> {

    /**
     * this can be replaced by other struct. such as mq
     */
    protected RingBuffer<StreamRecord<DiffStageRecords>> ringBuffer;
    private Set<ObjectPath> involvedTarget;

    public Output(RingBuffer<StreamRecord<DiffStageRecords>> ringBuffer,
                  Set<ObjectPath> involvedTarget) {
        this.ringBuffer = ringBuffer;
        this.involvedTarget = involvedTarget;
    }
    @Override
    public void collect(StreamRecord record) {
        DtsRecord value = (DtsRecord) record.value();
        ObjectPath target = value.getTarget();
        if (involvedTarget.contains(target)) {
            ringBuffer.publishEvent((event, sequence, sourceRecord) -> {
                event.value().setSource(sourceRecord);
            }, value);
        }
    }

    @Override
    public void close() {
    }
}
