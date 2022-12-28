package com.kronos.jobgraph.physic.operator.request;

import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.data.source.RowImage;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/**
 * @Author: jackila
 * @Date: 19:51 2022/12/19
 */
@Data
public class DeleteRequest extends DMLRequest {
    private String conditionFieldName;
    private Object value;

    public DeleteRequest(
            ObjectPath target,
            String conditionFieldName,
            Object value) {
        this.target = target;
        this.conditionFieldName = conditionFieldName;
        this.value = value;
    }

    public static DeleteRequest build(DtsRecord source) {
        RowImage rowImage = source.getBefore();
        return new DeleteRequest(rowImage.getTarget(), source.getPrimaryKey().getFieldName(),
                                 source.getPrimaryKey().getValue());
    }
}
