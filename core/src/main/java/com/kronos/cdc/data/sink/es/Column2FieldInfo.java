package com.kronos.cdc.data.sink.es;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 11:22 2020-10-12
 */
@Data
public class Column2FieldInfo {

    private ObjectPath defaultTable;
    private List<EsPrimaryFieldInfo> mapping;

    public Column2FieldInfo(ObjectPath defaultTable,
                            List<EsPrimaryFieldInfo> mapping) {
        this.defaultTable = defaultTable;
        this.mapping = mapping;
    }
}
