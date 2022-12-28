package com.kronos.jobgraph.table.table;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/**
 * @Author: jackila
 * @Date: 15:35 2022/12/27
 */
@Data
public class CatalogJDBCTable extends CatalogBaseTable{
    private ObjectPath target;

    public CatalogJDBCTable(ObjectPath target) {
        this.target = target;
    }
}
