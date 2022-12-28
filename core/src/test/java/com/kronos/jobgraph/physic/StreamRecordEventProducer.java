package com.kronos.jobgraph.physic;

import com.lmax.disruptor.RingBuffer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: jackila
 * @Date: 19:09 2022-12-15
 */
public class StreamRecordEventProducer {
    // 存储数据的环形队列
    private final RingBuffer<StreamRecord<List<String>>> ringBuffer;

    public StreamRecordEventProducer(RingBuffer<StreamRecord<List<String>>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(String content) {
        // ringBuffer是个队列，其next方法返回的是下最后一条记录之后的位置，这是个可用位置
        long sequence = ringBuffer.next();

        try {
            // sequence位置取出的事件是空事件
            StreamRecord<List<String>> event= ringBuffer.get(sequence);
            // 空事件添加业务信息
            String sender = "["+content+"]";
            CopyOnWriteArrayList<String> data = new CopyOnWriteArrayList<>();
            data.add(sender);
            event.setValue(data);
        } finally {
            // 发布
            ringBuffer.publish(sequence);
        }
    }
}
