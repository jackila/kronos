/*
 * Copyright 2022 Ververica Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.cdc.debezium;

import com.kronos.api.operators.Collector;
import com.kronos.api.tuple.Tuple2;
import com.kronos.cdc.data.RowKind;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.data.source.RecordField;
import com.kronos.cdc.data.source.RecordSchema;
import com.kronos.cdc.data.source.RowImage;
import com.kronos.cdc.data.source.SourceOffset;
import io.debezium.data.Envelope;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

/**
 * A JSON format implementation of {@link DebeziumDeserializationSchema} which deserializes the
 * received {@link SourceRecord} to JSON String.
 */
public class RowDebeziumDeserializationSchema implements DebeziumDeserializationSchema<DtsRecord> {
    private static final long serialVersionUID = 1L;

    @Override
    public void deserialize(SourceRecord record, Collector<DtsRecord> out) throws Exception {
        DtsRecord data = convertToDtsRecord(record);
        out.collect(data);
    }

    /**
     * debzium will handle primary key change
     *
     * @param record
     * @return
     */
    private DtsRecord convertToDtsRecord(SourceRecord record) {
        SourceOffset sourceOffset = new SourceOffset(record.sourceOffset());
        RowKind rowKind = RowKind.fromByteValue(Envelope.operationFor(record));

        Struct value = (Struct) record.value();
        Schema valueSchema = record.valueSchema();
        DtsRecord data = new DtsRecord(rowKind, sourceOffset);
        data.setPrimaryKey((Struct) record.key());
        switch (rowKind) {
            case INSERT:
                data.setAfter(extractAfterRow(value, valueSchema));
                data.setTarget(data.getAfter().getTarget());
                break;
            case DELETE:
                data.setBefore(extractBeforeRow(value, valueSchema));
                data.setTarget(data.getBefore().getTarget());
                break;
            case UPDATE:
                data.setAfter(extractAfterRow(value, valueSchema));
                data.setBefore(extractBeforeRow(value, valueSchema));
                // todo may be it change the tabelName
                data.setTarget(data.getAfter().getTarget());
                break;
        }

        return data;
    }

    private RowImage extractAfterRow(Struct value, Schema valueSchema) {
        Schema afterSchema = valueSchema.field(Envelope.FieldName.AFTER).schema();
        Struct after = value.getStruct(Envelope.FieldName.AFTER);

        return convert(afterSchema, after);
    }

    private RowImage extractBeforeRow(Struct value, Schema valueSchema) {
        Schema afterSchema = valueSchema.field(Envelope.FieldName.BEFORE).schema();
        Struct after = value.getStruct(Envelope.FieldName.BEFORE);

        return convert(afterSchema, after);
    }

    private RowImage convert(Schema schema, Struct value) {
        Tuple2<String, String> topicInfo = extractDatabaseAndTableName(schema.name());
        String database = topicInfo.f0;
        String tableName = topicInfo.f1;
        RecordSchema recordSchema = new RecordSchema(database, tableName);

        List<RecordField> records =
                schema.fields().stream()
                        .map(field -> new RecordField(field))
                        .collect(Collectors.toList());
        recordSchema.setRecordFields(records);
        RowImage rowImage = new RowImage(recordSchema, records.size());
        records.stream()
                .forEach(r -> rowImage.setValue(r.getFieldPosition(), value.get(r.getFieldName())));
        return rowImage;
    }

    // database,table
    private Tuple2<String, String> extractDatabaseAndTableName(String topic) {
        String[] info = topic.split("\\.");
        if (info.length != 4) {
            throw new RuntimeException("topic is not in debzium info type");
        }
        return Tuple2.of(info[1], info[2]);
    }
}
