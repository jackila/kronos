package com.kronos.jobgraph.physic.operator.request;

import com.kronos.cdc.data.ItemValue;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import lombok.Data;

/** */
@Data
public class QueryResponse {
    private List<ItemValue> data;
    private ObjectPath target;

    public QueryResponse(List<ItemValue> data, ObjectPath target) {
        this.data = data;
        this.target = target;
    }
}
