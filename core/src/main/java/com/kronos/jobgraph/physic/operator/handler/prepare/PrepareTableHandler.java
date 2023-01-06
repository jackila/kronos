package com.kronos.jobgraph.physic.operator.handler.prepare;

import com.kronos.cdc.data.AbstractTableItemRecord;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.PrePareRecord;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;

/** prepare stage : front stage + middle stage. */
public abstract class PrepareTableHandler
        extends AbstractTableTransformerHandler<StreamRecord<DiffStageRecords>> {

    @Override
    public QueryCondition findQueryCondition(DiffStageRecords eventVal) {
        return node.findQuerConditionInPrepare(eventVal.getPreTarget());
    }

    @Override
    protected AbstractTableItemRecord getItemRecord(DiffStageRecords value) {
        return value.getPrepareRecord();
    }

    @Override
    protected void initChainHead(StreamRecord<DiffStageRecords> event) {
        DiffStageRecords value = event.value();
        value.setPrepareRecord(PrePareRecord.build(value.getSource()));
    }

    @Override
    protected boolean isChainHead(StreamRecord<DiffStageRecords> event) {
        DiffStageRecords value = event.value();
        return value.getSourceEventTarget().equals(node.getTarget());
    }

    @Override
    protected boolean computeSourceTip() {
        return false;
    }
}
