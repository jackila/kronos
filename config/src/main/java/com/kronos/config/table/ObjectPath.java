package com.kronos.config.table;

import java.util.Objects;

/**
 * @Author: jackila
 * @Date: 7:20 PM 2022-6-23
 */
public class ObjectPath {
    private String databaseName;
    private String objectName;

    public ObjectPath(String databaseName, String objectName) {
        //todo check database objectname not null
        this.databaseName = databaseName;
        this.objectName = objectName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectPath that = (ObjectPath) o;
        return databaseName.equals(that.databaseName) && objectName.equals(that.objectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseName, objectName);
    }
}
