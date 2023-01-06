package com.kronos.cdc.data.source;

import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.ItemValue;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/** class contains values. */
public class RowImage {
    private final RecordSchema recordSchema;
    private final Object[] values;

    public RowImage(RecordSchema recordSchema, int fieldCount) {
        this.recordSchema = recordSchema;
        this.values = new Object[fieldCount];
    }

    public Object[] getValues() {
        return this.values;
    }

    public Object getValue(int index) {
        return values[index];
    }

    public Object getValue(RecordField field) {
        return getValue(field.getFieldPosition());
    }

    public ObjectPath getTarget() {
        return this.recordSchema.getTarget();
    }

    public Optional<Object> getValue(String fieldName) {
        Optional<RecordField> recordField = recordSchema.getField(fieldName);
        return recordField.map(field -> getValue(field));
    }

    public void setValue(String fieldName, Object value) {
        RecordField recordField = recordSchema.getField(fieldName).orElse(null);
        setValue(recordField, value);
    }

    public Collection<RecordField> fetchRecordFields() {
        return recordSchema.getRecordFields();
    }

    public void setValue(int i, Object value) {
        values[i] = value;
    }

    public void setValue(RecordField field, Object value) {
        int index = field.getFieldPosition();
        setValue(index, value);
    }

    public ItemValue toFieldItemMap() {
        Map<String, FieldItem> valueMap = new HashMap<>();

        ObjectPath target = this.recordSchema.getTarget();
        for (RecordField field : recordSchema.getFields()) {
            valueMap.put(
                    field.getFieldName(),
                    new FieldItem(target, field, values[field.getFieldPosition()]));
        }

        return new ItemValue(target, valueMap);
    }

    public Map<String, Object> toMap(
            Function<String, String> filedNameResolver, Function<Object, Object> valueResolver) {
        Map<String, Object> valueMap = new TreeMap<>();
        int i = 0;

        for (RecordField field : recordSchema.getFields()) {
            valueMap.put(
                    filedNameResolver == null
                            ? field.getFieldName()
                            : filedNameResolver.apply(field.getFieldName()),
                    valueResolver == null ? values[i] : valueResolver.apply(values[i]));
            i++;
        }

        return valueMap;
    }

    public Pair<RecordField, Object>[] buildFieldValuePairArray(
            Collection<RecordField> recordFields) {
        Pair<RecordField, Object>[] rs = new ImmutablePair[recordFields.size()];
        int index = 0;
        for (RecordField recordField : recordFields) {
            rs[index++] = Pair.of(recordField, getValue(recordField));
        }

        return rs;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("[");

        recordSchema
                .getFields()
                .forEach(
                        recordField -> {
                            sb.append("Field ")
                                    .append("[")
                                    .append(recordField.getFieldName())
                                    .append("]")
                                    .append(" ")
                                    .append("[")
                                    .append(getValue(recordField))
                                    .append("]")
                                    .append("\n");
                        });

        sb.append("]");

        return sb.toString();
    }
}
