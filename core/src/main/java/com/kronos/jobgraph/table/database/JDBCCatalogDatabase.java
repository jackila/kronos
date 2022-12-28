package com.kronos.jobgraph.table.database;

import com.google.common.collect.Lists;
import com.kronos.jobgraph.raw.DataSource;
import com.kronos.jobgraph.table.DatabaseType;
import com.kronos.jobgraph.table.ObjectPath;
import com.kronos.jobgraph.table.table.CatalogJDBCTable;
import lombok.Data;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 15:25 2022/12/27
 */
@Data
public class JDBCCatalogDatabase extends CatalogDatabase {
    private String database;

    private List<CatalogJDBCTable> tables;

    public JDBCCatalogDatabase() {
        this.tables = Lists.newArrayList();
    }

    public static CatalogDatabase build(DataSource dataSource) {
        JDBCCatalogDatabase ret = new JDBCCatalogDatabase();
        ret.setDatabase(dataSource.getSchema());
        ret.setAddress(dataSource.getUrl());
        ret.setUsername(dataSource.getUser());
        ret.setPassword(dataSource.getPassword());
        ret.setDatabaseType(DatabaseType.MYSQL);
        return ret;
    }

    @Override
    public String specificName() {
        return database;
    }

    @Override
    public void registerTable(ObjectPath objectPath) {
        tables.add(new CatalogJDBCTable(objectPath));
    }

    @Override
    public boolean checkTable(String table) {
        return tables.stream().filter(t -> table.equalsIgnoreCase(t.getTarget().getObjectName())).findAny().isPresent();
    }
}
