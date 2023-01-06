package com.kronos.cdc.data;

import com.kronos.cdc.data.source.RecordField;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** */
@Getter
@Setter
public class FieldItem {
    private ObjectPath source;
    private RecordField field;
    private Object value;

    public FieldItem(ObjectPath source, RecordField field, Object value) {
        this.source = source;
        this.field = field;
        this.value = value;
    }

    public String getFieldName() {
        return field.getFieldName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }
        FieldItem item = (FieldItem) o;

        return new EqualsBuilder()
                .append(String.valueOf(this.value), String.valueOf(item.value))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(value).toHashCode();
    }
}
