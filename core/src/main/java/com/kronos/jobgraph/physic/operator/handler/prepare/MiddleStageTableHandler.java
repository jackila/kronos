package com.kronos.jobgraph.physic.operator.handler.prepare;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.ItemValue;
import com.kronos.cdc.data.SinkerRecord;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.handler.StageType;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;
import com.kronos.jobgraph.table.ObjectPath;
import com.lmax.disruptor.RingBuffer;
import java.util.List;

/** */
public class MiddleStageTableHandler extends PrepareTableHandler {
    private RingBuffer<StreamRecord<DiffStageRecords>> ringBuffer;

    @Override
    public void onEvent(StreamRecord<DiffStageRecords> event, long sequence, boolean endOfBatch)
            throws Exception {

        if (doHandler(event)) {
            QueryResponse response = searchItems(event);
            if (response == null) {
                return;
            }
            List<ItemValue> data = response.getData();
            if (data == null || data.isEmpty()) {
                return;
            }
            ObjectPath target = node.getTarget();
            DiffStageRecords value = event.value();
            for (ItemValue datum : data) {
                sendToSinker(value.partClone(target, datum));
            }
        } else if (isChainHead(event)) {
            initChainHead(event);
            sendToSinker(event.value());
        }
    }

    @Override
    protected void initChainHead(StreamRecord<DiffStageRecords> event) {
        DiffStageRecords value = event.value();
        value.setSinkerRecord(SinkerRecord.build(value.getSource()));
    }

    private void sendToSinker(DiffStageRecords records) {
        ringBuffer.publishEvent(
                (sinkEvent, sinkSequence, sinkeSourceRecord) ->
                        sinkEvent.setValue(sinkeSourceRecord),
                records);
    }

    @Override
    public void catchEventChange(StreamRecord<DiffStageRecords> event) {}

    public void setSinkerRingBuffer(RingBuffer<StreamRecord<DiffStageRecords>> sinkerRingBuffer) {
        this.ringBuffer = sinkerRingBuffer;
    }

    @Override
    public boolean doHandler(StreamRecord<DiffStageRecords> event) {
        return event.value().doHandler(node, StageType.MIDDLE);
    }
}
