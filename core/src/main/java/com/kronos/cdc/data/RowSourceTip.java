package com.kronos.cdc.data;

import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/** 所谓的【来源】是指该数据通过什么表中哪个字段的值获取到的 */
@Data
public class RowSourceTip {

    /** 数据来源表 */
    private ObjectPath sourceTable;

    /** 来源字段 */
    private String sourceColumn;

    /** 来源字段对应的值 */
    private Object sourceValue;

    public RowSourceTip(QueryCondition condition) {
        this.sourceTable = condition.getFindTarget();
        this.sourceColumn = condition.getFindField();
    }

    public RowSourceTip(RowSourceTip tip) {
        this.sourceTable = tip.getSourceTable();
        this.sourceColumn = tip.getSourceColumn();
    }
}
