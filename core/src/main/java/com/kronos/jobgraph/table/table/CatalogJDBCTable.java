package com.kronos.jobgraph.table.table;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/** */
@Data
public class CatalogJDBCTable extends CatalogBaseTable {
    private ObjectPath target;

    public CatalogJDBCTable(ObjectPath target) {
        this.target = target;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(target.getDatabaseName());
        sb.append(".");
        sb.append(target.getObjectName());
        return sb.toString();
    }
}
