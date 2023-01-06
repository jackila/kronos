package com.kronos.jobgraph.physic.operator.db;

import com.kronos.jobgraph.physic.operator.request.DeleteRequest;
import com.kronos.jobgraph.physic.operator.request.DeleteResponse;
import com.kronos.jobgraph.physic.operator.request.InsertRequest;
import com.kronos.jobgraph.physic.operator.request.InsertResponse;
import com.kronos.jobgraph.physic.operator.request.QueryRequest;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;

/** 常见使用模式如下 rocks db jdbc rpc/https others */
public interface DataWarehouseManager {
    QueryResponse select(QueryRequest request);

    InsertResponse insert(InsertRequest request);

    DeleteResponse delete(DeleteRequest request);
}
