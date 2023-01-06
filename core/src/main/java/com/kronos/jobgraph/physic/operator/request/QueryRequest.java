package com.kronos.jobgraph.physic.operator.request;

import com.kronos.cdc.data.AbstractTableItemRecord;
import com.kronos.cdc.data.RowSourceTip;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** */
@Data
@Builder
public class QueryRequest {

    private ObjectPath target;

    // todo need field names
    private List<String> fieldNames;
    private String conditionFieldName;
    private List<Object> value;
    private RowSourceTip sourceTip;

    public QueryRequest() {}

    public QueryRequest(
            ObjectPath target,
            List<String> fieldNames,
            String conditionFieldName,
            List<Object> value,
            RowSourceTip sourceTip) {
        this.target = target;
        this.fieldNames = fieldNames;
        this.conditionFieldName = conditionFieldName;
        this.value = value;
        this.sourceTip = sourceTip;
    }

    public QueryRequest(ObjectPath target, String conditionFieldName, List<Object> value) {
        this.target = target;
        this.conditionFieldName = conditionFieldName;
        this.value = value;
    }

    public static QueryRequest newInstance(
            AbstractTableItemRecord record,
            QueryCondition condition,
            ObjectPath target,
            boolean sourceTips) {
        List<Object> value = record.find(condition);
        QueryRequestBuilder requestBuilder =
                new QueryRequestBuilder()
                        .target(target)
                        .conditionFieldName(condition.getField())
                        .value(value);
        if (sourceTips) {
            requestBuilder.sourceTip(new RowSourceTip(condition));
        }
        return requestBuilder.build();
    }
}
