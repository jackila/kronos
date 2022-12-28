package com.kronos.jobgraph.physic.operator.db;

import com.kronos.jobgraph.physic.operator.request.DeleteRequest;
import com.kronos.jobgraph.physic.operator.request.DeleteResponse;
import com.kronos.jobgraph.physic.operator.request.InsertRequest;
import com.kronos.jobgraph.physic.operator.request.InsertResponse;
import com.kronos.jobgraph.physic.operator.request.QueryRequest;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;

/**
 * @Author: jackila
 * @Date: 15:00 2022/12/20
 */
public class JDBCWarehouseManager implements DataWarehouseManager{
    @Override
    public QueryResponse select(QueryRequest request) {
        return null;
    }

    @Override
    public InsertResponse insert(InsertRequest request) {
        return null;
    }

    @Override
    public DeleteResponse delete(DeleteRequest request) {
        return null;
    }
}
