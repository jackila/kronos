package com.kronos.jobgraph.raw;

/**
 * @Author: jackila
 * @Date: 11:07 AM 2022-5-29
 */
public class TableInfo {
    private String tableName;
    private String database;
    private boolean mainTable;

    public TableInfo(String tableName,
                     String database,
                     boolean mainTable) {
        this.tableName = tableName;
        this.database = database;
        this.mainTable = mainTable;
    }

    public TableInfo(String tableName,
                     String database) {
        this.tableName = tableName;
        this.database = database;
    }

    public TableInfo() {
    }

    public TableInfo(String table, boolean mainTable) {
        String[] split = table.split("\\.");
        if(split.length != 2){
            throw new RuntimeException("the table info should be database.tableName");
        }
        this.database = split[0].trim();
        this.tableName = split[1].trim();
        this.mainTable = mainTable;
    }

    public TableInfo(String table) {
       this(table,false);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        String[] info = tableName.split("\\.");
        if(info.length == 2){
            this.database = info[0].trim();
            this.tableName = info[1].trim();
        }else{
            this.tableName = tableName;
        }
    }

    public boolean isMainTable() {
        return mainTable;
    }

    public void setMainTable(boolean mainTable) {
        this.mainTable = mainTable;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableInfo tableInfo = (TableInfo) o;

        if (!tableName.equals(tableInfo.tableName)) return false;
        return database.equals(tableInfo.database);
    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + database.hashCode();
        return result;
    }
}
