package com.kronos.jobgraph.physic.operator.handler;

import com.kronos.cdc.data.AbstractTableItemRecord;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.db.DataWarehouseManager;
import com.kronos.jobgraph.physic.operator.request.QueryRequest;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;
import com.lmax.disruptor.EventHandler;
import lombok.SneakyThrows;

/** */
public abstract class AbstractTableTransformerHandler<S> implements EventHandler<S> {
    protected TPhysicalNode node;
    protected DataWarehouseManager wareHouseManager;

    public TPhysicalNode getNode() {
        return node;
    }

    @Override
    public void onEvent(S event, long sequence, boolean endOfBatch) throws Exception {
        if (doHandler(event)) {
            catchEventChange(event);
        } else if (isChainHead(event)) {
            initChainHead(event);
        }
    }

    protected QueryResponse searchItems(StreamRecord<DiffStageRecords> event) {
        DiffStageRecords value = event.value();

        QueryCondition condition = findQueryCondition(value);
        AbstractTableItemRecord record = getItemRecord(value);
        boolean sourceTips = computeSourceTip();

        QueryRequest request =
                QueryRequest.newInstance(record, condition, node.getTarget(), sourceTips);
        // request data item
        return wareHouseManager.select(request);
    }

    protected boolean computeSourceTip() {
        return true;
    }

    protected void initChainHead(S event) {}

    protected boolean isChainHead(S event) {
        return false;
    }

    public void catchEventChange(S event) {}

    public QueryCondition findQueryCondition(DiffStageRecords value) {
        return null;
    }

    protected AbstractTableItemRecord getItemRecord(DiffStageRecords value) {
        return null;
    }

    public AbstractTableTransformerHandler setNode(TPhysicalNode node) {
        this.node = node;
        return this;
    }

    public void setWareHouseManager(DataWarehouseManager wareHouseManager) {
        this.wareHouseManager = wareHouseManager;
    }

    public boolean doHandler(S event) {
        return true;
    }

    @SneakyThrows
    public AbstractTableTransformerHandler clone(TPhysicalNode node) {
        AbstractTableTransformerHandler handler = this.getClass().newInstance();
        handler.setNode(node);
        return handler;
    }
}
