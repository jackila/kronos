package com.kronos.cdc.data.sink.es;

import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import lombok.Data;

/** */
@Data
public class Column2FieldInfo {

    private ObjectPath defaultTable;
    private List<EsPrimaryFieldInfo> mapping;

    public Column2FieldInfo(ObjectPath defaultTable, List<EsPrimaryFieldInfo> mapping) {
        this.defaultTable = defaultTable;
        this.mapping = mapping;
    }
}
