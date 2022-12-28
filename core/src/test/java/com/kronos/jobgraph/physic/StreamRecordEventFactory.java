package com.kronos.jobgraph.physic;

import com.lmax.disruptor.EventFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: jackila
 * @Date: 18:59 2022-12-15
 */
public class StreamRecordEventFactory implements EventFactory<StreamRecord<List<String>>> {
    @Override
    public StreamRecord<List<String>> newInstance() {
        return new StreamRecord<>(new CopyOnWriteArrayList<>());
    }
}
