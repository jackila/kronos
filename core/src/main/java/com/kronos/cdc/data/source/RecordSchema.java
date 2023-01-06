package com.kronos.cdc.data.source;

import com.kronos.jobgraph.table.ObjectPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.connect.data.Schema;

/** schema. */
@Setter
@Getter
public class RecordSchema {
    private final String databaseName;
    private final String tableName;
    private final ObjectPath target;
    private List<RecordField> recordFields;
    private Map<String, Integer> recordFieldIndex;

    public RecordSchema(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.target = new ObjectPath(databaseName, tableName);
    }

    public void setRecordFields(List<RecordField> recordFields) {
        if (null != recordFields && !recordFields.isEmpty()) {

            this.recordFields = new ArrayList<>(recordFields.size());
            this.recordFieldIndex = new HashMap<>();
            for (RecordField field : recordFields) {
                this.recordFields.add(field);
                this.recordFieldIndex.put(field.getFieldName(), field.getFieldPosition());
            }
        }
    }

    public List<RecordField> getFields() {
        return this.recordFields;
    }

    public int getFieldCount() {
        return getFieldCount(true);
    }

    public int getFieldCount(boolean load) {
        return null == this.recordFields ? -1 : this.recordFields.size();
    }

    public RecordField getField(int index) {
        return this.recordFields.get(index);
    }

    public Optional<RecordField> getField(String fieldName) {
        return this.getField(fieldName, true);
    }

    public Optional<RecordField> getField(String fieldName, boolean load) {
        Integer index = this.recordFieldIndex.get(fieldName);
        if (null == index) {
            return Optional.empty();
        }
        return Optional.of(this.recordFields.get(index));
    }

    public List<Schema.Type> getRawDataTypes() {
        return recordFields.stream().map(RecordField::getType).collect(Collectors.toList());
    }

    public List<String> getFieldNames() {
        return recordFields.stream().map(RecordField::getFieldName).collect(Collectors.toList());
    }

    public Optional<Schema.Type> getRawDataType(String fieldName) {
        RecordField recordField = getField(fieldName).get();
        if (null == recordField) {
            return Optional.empty();
        } else {
            return Optional.of(recordField.getType());
        }
    }
}
