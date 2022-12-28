package com.kronos.cdc.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.data.source.RowImage;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;
import com.kronos.jobgraph.table.ObjectPath;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author: jackila
 * @Date: 13:50 2022/12/19
 */
public abstract class AbstractTableItemRecord implements Record {
    protected ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> items;

    public List<Object> find(QueryCondition condition) {
        List<Map<String, FieldItem>> tableItems = items.getOrDefault(condition.getFindTarget(), Lists.newArrayList());
        return tableItems.stream().map(t -> t.getOrDefault(condition.getFindField(), null)).collect(Collectors.toList());
    }

    public void addItem(QueryResponse response) {
        if(response == null){
            return;
        }
        List<Map<String, FieldItem>> data = response.getData();
        if (data == null || data.isEmpty()) {
            return;
        }
        items.put(response.getTarget(), data);
    }

    protected ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> init(DtsRecord record) {
        ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> ret = new ConcurrentHashMap<>();
        Map<String, FieldItem> data = Maps.newHashMap();
        switch (record.getType()) {
            case INSERT:
            case UPDATE:
                data = convert(record.getAfter());
                break;
            case DELETE:
                data = convert(record.getBefore());
        }
        ret.put(record.getTarget(), Lists.newArrayList(data));
        return ret;
    }

    private Map<String, FieldItem> convert(RowImage row) {
        return row.toFieldItemMap();
    }

    public ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> getItems() {
        return items;
    }
}
