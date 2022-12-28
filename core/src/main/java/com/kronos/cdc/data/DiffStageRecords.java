package com.kronos.cdc.data;

import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.handler.StageType;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @Author: jackila
 * @Date: 12:55 2022/12/18
 */
@Getter
@Setter
public class DiffStageRecords implements Record {
    public DiffStageRecords(DtsRecord source) {
        this.source = source;
    }

    public DiffStageRecords() {
    }

    private DtsRecord source;
    private ControllerRecord controller;

    private PrePareRecord prepareRecord;

    /**
     * use by backstage and sinker operator
     */
    private SinkerRecord sinkerRecord;

    public RowKind getEventType(){
        return source.getType();
    }
    public boolean doHandler(TPhysicalNode target,
                             StageType type) {
        return controller.doHandle(target, type);
    }

    public ObjectPath getPreTarget() {
        return controller.getPreTarget();
    }

    public ObjectPath getSourceEventTarget() {
        return source.getTarget();
    }

    public DiffStageRecords partClone(ObjectPath target,
                                      Map<String, FieldItem> item) {
        ControllerRecord controllerRecord = new ControllerRecord(controller);
        DiffStageRecords records = new DiffStageRecords();
        records.setController(controllerRecord);
        records.setSinkerRecord(new SinkerRecord(target, item));
        return records;
    }
}
