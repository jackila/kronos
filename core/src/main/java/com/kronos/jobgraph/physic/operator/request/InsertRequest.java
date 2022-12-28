package com.kronos.jobgraph.physic.operator.request;

import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.data.source.RecordField;
import com.kronos.cdc.data.source.RowImage;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @Author: jackila
 * @Date: 19:18 2022/12/19
 */
@Data
public class InsertRequest extends DMLRequest {
    public InsertRequest(ObjectPath target,
                         Pair<RecordField, Object>[] value) {
        this.value = value;
        this.target = target;
    }

    private Pair<RecordField, Object>[] value;

    public static InsertRequest build(DtsRecord source) {
        ObjectPath target = source.getTarget();
        RowImage rowImage = source.getAfter();
        Pair<RecordField, Object>[] value = rowImage.buildFieldValuePairArray(rowImage.fetchRecordFields());

        return new InsertRequest(target, value);
    }
}
