package com.kronos.cdc.data.source;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;

/** record id replsent as database column. */
public class RecordField {
    private final String fieldName;
    private final Schema.Type type;
    private boolean isPrimaryKey;
    private boolean isUniqueKey;
    private int fieldPosition;

    public RecordField(String fieldName, Schema.Type rawDataType,int fieldPosition) {
        this.fieldName = fieldName;
        this.type = rawDataType;
        this.fieldPosition = fieldPosition;
    }

    public RecordField(Field field) {
        this.fieldName = field.name();
        this.type = field.schema().type();
        this.fieldPosition = field.index();
    }

    public String getFieldName() {
        return fieldName;
    }

    public Schema.Type getType() {
        return type;
    }

    public void setSourceRawDataType(RawDataType rawDataType) {
        throw new RuntimeException("does not support this function");
    }

    public Object getDefaultValue() {
        return null;
    }

    public boolean isNullable() {
        return true;
    }

    public boolean isUnique() {
        return isUniqueKey;
    }

    public RecordField setUnique(boolean isUnique) {
        isUniqueKey = isUnique;
        return this;
    }

    public boolean isPrimary() {
        return isPrimaryKey;
    }

    public boolean setPrimary(boolean isPrimary) {
        isPrimaryKey = isPrimary;
        return isPrimaryKey;
    }

    public boolean isIndexed() {
        return isPrimaryKey || isUniqueKey;
    }

    public boolean isAutoIncrement() {
        return false;
    }

    public int keySeq() {
        return 0;
    }

    public int getFieldPosition() {
        return fieldPosition;
    }

    public void setFieldPosition(int fieldPosition) {
        this.fieldPosition = fieldPosition;
    }

    public int getDisplaySize() {
        return 0;
    }

    public int getScale() {
        return 0;
    }
}
