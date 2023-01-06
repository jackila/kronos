package com.kronos.jobgraph.physic.operator.handler.prepare;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.handler.StageType;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;

/** */
public class FrontStageTableHandler extends PrepareTableHandler {
    @Override
    public void catchEventChange(StreamRecord<DiffStageRecords> event) {
        QueryResponse response = searchItems(event);
        // set it into record
        event.value().getPrepareRecord().addItem(response);
    }

    @Override
    public boolean doHandler(StreamRecord<DiffStageRecords> event) {
        return event.value().doHandler(node, StageType.FRONT);
    }
}
