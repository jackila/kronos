package com.kronos.jobgraph.physic.operator.handler;

import com.kronos.cdc.data.AbstractTableItemRecord;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;

/**
 * @Author: jackila
 * @Date: 11:29 2022-12-15
 */
public class BackStageTableHandler extends AbstractTableTransformerHandler<StreamRecord<DiffStageRecords>> {
    @Override
    public void catchEventChange(StreamRecord<DiffStageRecords> event) {
        QueryResponse response = searchItems(event);
        // set it into record
        event.value().getSinkerRecord().addItem(response);
    }
    @Override
    public QueryCondition findQueryCondition(DiffStageRecords target) {
        return node.getParentRelevanceUsedInBackStage();
    }

    @Override
    protected AbstractTableItemRecord getItemRecord(DiffStageRecords value) {
        return value.getSinkerRecord();
    }
}
