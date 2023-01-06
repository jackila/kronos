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

package com.kronos.cdc.source.mysql.source.reader;

import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.getBinlogPosition;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.getHistoryRecord;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.getWatermark;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.isDataChangeRecord;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.isHeartbeatEvent;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.isHighWatermarkEvent;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.isSchemaChangeEvent;
import static com.kronos.cdc.source.mysql.source.utils.RecordUtils.isWatermarkEvent;

import com.kronos.api.operators.Collector;
import com.kronos.cdc.debezium.DebeziumDeserializationSchema;
import com.kronos.cdc.debezium.history.FlinkJsonTableChangeSerializer;
import com.kronos.cdc.source.base.source.reader.RecordEmitter;
import com.kronos.cdc.source.mysql.source.offset.BinlogOffset;
import com.kronos.cdc.source.mysql.source.split.MySqlSplitState;
import com.kronos.cdc.source.mysql.source.split.SourceRecords;
import com.kronos.jobgraph.physic.operator.source.SourceOutput;
import io.debezium.document.Array;
import io.debezium.relational.history.HistoryRecord;
import io.debezium.relational.history.TableChanges;
import java.util.Iterator;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RecordEmitter} implementation for {@link MySqlSourceReader}.
 *
 * <p>The {@link RecordEmitter} buffers the snapshot records of split and call the binlog reader to
 * emit records rather than emit the records directly.
 */
public final class MySqlRecordEmitter<T>
        implements RecordEmitter<SourceRecords, T, MySqlSplitState> {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlRecordEmitter.class);
    private static final FlinkJsonTableChangeSerializer TABLE_CHANGE_SERIALIZER =
            new FlinkJsonTableChangeSerializer();

    private final DebeziumDeserializationSchema<T> debeziumDeserializationSchema;
    private final boolean includeSchemaChanges;
    private final OutputCollector<T> outputCollector;

    public MySqlRecordEmitter(
            DebeziumDeserializationSchema<T> debeziumDeserializationSchema,
            boolean includeSchemaChanges) {
        this.debeziumDeserializationSchema = debeziumDeserializationSchema;
        this.includeSchemaChanges = includeSchemaChanges;
        this.outputCollector = new OutputCollector<>();
    }

    @Override
    public void emitRecord(
            SourceRecords sourceRecords, SourceOutput<T> output, MySqlSplitState splitState)
            throws Exception {
        final Iterator<SourceRecord> elementIterator = sourceRecords.iterator();
        while (elementIterator.hasNext()) {
            processElement(elementIterator.next(), output, splitState);
        }
    }

    private void processElement(
            SourceRecord element, SourceOutput<T> output, MySqlSplitState splitState)
            throws Exception {
        if (isWatermarkEvent(element)) {
            BinlogOffset watermark = getWatermark(element);
            if (isHighWatermarkEvent(element) && splitState.isSnapshotSplitState()) {
                splitState.asSnapshotSplitState().setHighWatermark(watermark);
            }
        } else if (isSchemaChangeEvent(element) && splitState.isBinlogSplitState()) {
            HistoryRecord historyRecord = getHistoryRecord(element);
            Array tableChanges =
                    historyRecord.document().getArray(HistoryRecord.Fields.TABLE_CHANGES);
            TableChanges changes = TABLE_CHANGE_SERIALIZER.deserialize(tableChanges, true);
            for (TableChanges.TableChange tableChange : changes) {
                splitState.asBinlogSplitState().recordSchema(tableChange.getId(), tableChange);
            }
            if (includeSchemaChanges) {
                BinlogOffset position = getBinlogPosition(element);
                splitState.asBinlogSplitState().setStartingOffset(position);
                emitElement(element, output);
            }
        } else if (isDataChangeRecord(element)) {
            updateStartingOffsetForSplit(splitState, element);
            emitElement(element, output);
        } else if (isHeartbeatEvent(element)) {
            updateStartingOffsetForSplit(splitState, element);
        } else {
            // unknown element
            LOG.info("Meet unknown element {}, just skip.", element);
        }
    }

    private void updateStartingOffsetForSplit(MySqlSplitState splitState, SourceRecord element) {
        if (splitState.isBinlogSplitState()) {
            BinlogOffset position = getBinlogPosition(element);
            splitState.asBinlogSplitState().setStartingOffset(position);
        }
    }

    private void emitElement(SourceRecord element, SourceOutput<T> output) throws Exception {
        outputCollector.output = output;
        debeziumDeserializationSchema.deserialize(element, outputCollector);
    }

    private static class OutputCollector<T> implements Collector<T> {
        private SourceOutput<T> output;

        @Override
        public void collect(T record) {
            output.collect(record);
        }

        @Override
        public void close() {
            // do nothing
        }
    }
}
