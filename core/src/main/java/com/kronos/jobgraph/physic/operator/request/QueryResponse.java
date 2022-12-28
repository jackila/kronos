package com.kronos.jobgraph.physic.operator.request;

import com.kronos.cdc.data.FieldItem;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: jackila
 * @Date: 13:40 2022/12/19
 */
@Data
public class QueryResponse {
    private List<Map<String, FieldItem>> data;
    private ObjectPath target;

    public QueryResponse(List<Map<String, FieldItem>> data,
                         ObjectPath target) {
        this.data = data;
        this.target = target;
    }
}
