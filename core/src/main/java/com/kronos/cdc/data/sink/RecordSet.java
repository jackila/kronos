package com.kronos.cdc.data.sink;

import com.kronos.cdc.data.ControllerRecord;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.SinkerRecord;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: jackila
 * @Date: 05:26 2022/12/27
 */
@Data
public class RecordSet {
    private SinkerRecord record;
    private ControllerRecord controller;
    private FieldItem primaryKey;

    public RecordSet(SinkerRecord record,
                     ControllerRecord controller) {
        this.record = record;
        this.controller = controller;
    }

    public RecordSet(DiffStageRecords value) {
        this.record = value.getSinkerRecord();
        this.controller = value.getController();
        this.primaryKey = value.getController().getPrimaryKey();
    }

    public ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> getItems() {
        return this.record.getItems();
    }
}
