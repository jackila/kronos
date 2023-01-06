package com.kronos.cdc.data.sink;

import com.kronos.cdc.data.ControllerRecord;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.ItemValue;
import com.kronos.cdc.data.SinkerRecord;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

/** */
@Data
public class RecordSet {
    private SinkerRecord record;
    private ControllerRecord controller;
    private FieldItem primaryKey;

    public RecordSet(SinkerRecord record, ControllerRecord controller) {
        this.record = record;
        this.controller = controller;
    }

    public RecordSet(DiffStageRecords value) {
        this.record = value.getSinkerRecord();
        this.controller = value.getController();
        this.primaryKey = value.getController().getPrimaryKey();
    }

    public ConcurrentHashMap<ObjectPath, List<ItemValue>> getItems() {
        return this.record.getItems();
    }
}
