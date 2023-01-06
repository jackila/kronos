package com.kronos.jobgraph.physic.operator.handler;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.request.DeleteRequest;
import com.kronos.jobgraph.physic.operator.request.InsertRequest;

/** */
public class ModifyWareHouseHandler
        extends AbstractTableTransformerHandler<StreamRecord<DiffStageRecords>> {
    private boolean local = true;

    @Override
    public void catchEventChange(StreamRecord<DiffStageRecords> event) {
        if (local) {
            DiffStageRecords value = event.value();
            switch (value.getEventType()) {
                case INSERT:
                    // insert data
                    wareHouseManager.insert(InsertRequest.build(value.getSource()));
                    break;
                case DELETE:
                    wareHouseManager.delete(DeleteRequest.build(value.getSource()));
                    // delete data
                    break;
                case UPDATE:
                    // delete data
                    wareHouseManager.delete(DeleteRequest.build(value.getSource()));
                    // insert data
                    wareHouseManager.insert(InsertRequest.build(value.getSource()));
                    break;
            }
        }
    }

    @Override
    public boolean doHandler(StreamRecord<DiffStageRecords> event) {
        return true;
    }
}
