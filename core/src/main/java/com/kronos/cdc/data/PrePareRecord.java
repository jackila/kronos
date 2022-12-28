package com.kronos.cdc.data;

import com.kronos.cdc.data.source.DtsRecord;

/**
 * @Author: jackila
 * @Date: 11:36 2022/12/19
 */
public class PrePareRecord extends AbstractTableItemRecord{
    public PrePareRecord(DtsRecord record) {
        this.items = init(record);
    }
    public static PrePareRecord build(DtsRecord record) {
        return new PrePareRecord(record);
    }

}
