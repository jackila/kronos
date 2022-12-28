package com.kronos.jobgraph.table.database;

import com.kronos.jobgraph.table.DatabaseType;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/**
 * 连接信息
 * @Author: jackila
 * @Date: 11:46 AM 2022-6-22
 */
@Data
public abstract class CatalogDatabase {

    protected String address;
    protected String username;
    protected String password;

    protected DatabaseType databaseType;

    protected boolean sinker;

    public CatalogDatabase() {
    }

    public abstract String specificName();

    public abstract void registerTable(ObjectPath objectPath);

    public abstract boolean checkTable(String table);
}
