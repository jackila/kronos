package com.kronos.cdc.data;

import com.kronos.cdc.data.source.DtsRecord;

/** */
public class PrePareRecord extends AbstractTableItemRecord {
    public PrePareRecord(DtsRecord record) {
        this.items = init(record);
    }

    public static PrePareRecord build(DtsRecord record) {
        return new PrePareRecord(record);
    }
}
