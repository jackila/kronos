package com.kronos.jobgraph.physic.operator.request;

import com.kronos.cdc.data.AbstractTableItemRecord;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 13:38 2022/12/19
 */
@Getter
@Setter
public class QueryRequest {

    private ObjectPath target;

    //todo need field names
    private List<String> fieldNames;
    private String conditionFieldName;
    @NonNull
    private List<Object> value;

    public QueryRequest(ObjectPath target,
                        String conditionFieldName,
                        @NonNull List<Object> value) {
        this.target = target;
        this.conditionFieldName = conditionFieldName;
        this.value = value;
    }

    public static QueryRequest newInstance(AbstractTableItemRecord record,
                                           QueryCondition condition,
                                           ObjectPath target) {
        List<Object> value = record.find(condition);
        return new QueryRequest(target, condition.getField(), value);
    }
}
