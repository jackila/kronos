package com.kronos.jobgraph.physic.operator;

import com.kronos.jobgraph.physic.StreamRecord;

/**
 * @Author: jackila
 * @Date: 11:34 AM 2022-7-24
 */
public abstract class DataStreamSource<T> {
    private int operatorId;
    private String operatorName;

    private StreamTaskContext<T> context;

    private boolean running;

    public DataStreamSource(StreamTaskContext context) {
        this.running = true;
        this.context = context;
    }

    public abstract T fetchData();

    public void cancel() {

    }
}
