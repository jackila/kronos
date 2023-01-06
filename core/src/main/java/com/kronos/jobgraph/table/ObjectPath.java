package com.kronos.jobgraph.table;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;

/** */
public class ObjectPath {
    private String databaseName;
    private String objectName;

    public ObjectPath(String databaseName, String objectName) {
        // todo check database objectname not null
        this.databaseName = databaseName;
        this.objectName = objectName;
    }

    @VisibleForTesting
    public ObjectPath(String name) {
        this.databaseName = name;
        this.objectName = name;
    }

    public ObjectPath() {}

    public String getDatabaseName() {
        return databaseName;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObjectPath that = (ObjectPath) o;
        return databaseName.equals(that.databaseName) && objectName.equals(that.objectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseName, objectName);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(databaseName);
        sb.append(".");
        sb.append(objectName);
        return sb.toString();
    }
}
