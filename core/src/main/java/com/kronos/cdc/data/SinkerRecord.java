package com.kronos.cdc.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.concurrent.ConcurrentHashMap;

/** */
public class SinkerRecord extends AbstractTableItemRecord {
    public SinkerRecord(DtsRecord record) {
        this.items = init(record);
    }

    public static SinkerRecord build(DtsRecord record) {
        return new SinkerRecord(record);
    }

    public SinkerRecord(ObjectPath target, ItemValue item) {
        if (items == null) {
            items = new ConcurrentHashMap<>();
        }

        items.put(target, Lists.newArrayList(item));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SinkerRecord{");
        sb.append("items=").append(new Gson().toJson(items));
        sb.append('}');
        return sb.toString();
    }
}
