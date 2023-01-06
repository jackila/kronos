package com.kronos.cdc.data;

import com.google.common.collect.Lists;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.data.source.RowImage;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** abstract. */
public abstract class AbstractTableItemRecord implements Record {
    protected ConcurrentHashMap<ObjectPath, List<ItemValue>> items;

    public List<Object> find(QueryCondition condition) {
        ObjectPath findTarget = condition.getFindTarget();
        if (items.containsKey(findTarget)) {
            List<ItemValue> itemValues = items.get(findTarget);
            return itemValues.stream()
                    .map(t -> t.getColumnValues().getOrDefault(condition.getFindField(), null))
                    .collect(Collectors.toList());
        } else {
            return Lists.newArrayList();
        }
    }

    public void addItem(QueryResponse response) {
        if (response == null) {
            return;
        }
        List<ItemValue> data = response.getData();
        if (data == null || data.isEmpty()) {
            return;
        }
        items.put(response.getTarget(), data);
    }

    protected ConcurrentHashMap<ObjectPath, List<ItemValue>> init(DtsRecord record) {
        ConcurrentHashMap<ObjectPath, List<ItemValue>> ret = new ConcurrentHashMap<>();
        ItemValue data = null;
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

    private ItemValue convert(RowImage row) {
        return row.toFieldItemMap();
    }

    public ConcurrentHashMap<ObjectPath, List<ItemValue>> getItems() {
        return items;
    }
}
