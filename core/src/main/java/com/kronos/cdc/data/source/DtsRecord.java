package com.kronos.cdc.data.source;

import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.Record;
import com.kronos.cdc.data.RowKind;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

/** */
@Getter
@Setter
public class DtsRecord implements Record {

    private RowKind type;
    private RowImage before;
    private RowImage after;
    private SourceOffset sourcePosition;
    private ObjectPath target;
    private FieldItem primaryKey;

    public DtsRecord(RowKind rowKind, SourceOffset sourceOffset) {
        this.type = rowKind;
        this.sourcePosition = sourceOffset;
    }

    public void setPrimaryKey(Struct key) {
        Schema schema = key.schema();
        Field field = schema.fields().get(0);
        Object value = key.get(field);
        RecordField recordField = new RecordField(field);
        this.primaryKey = new FieldItem(target, recordField, value);
    }
}
