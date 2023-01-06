package com.kronos.cdc.source.mysql.debezium.task.context;

import io.debezium.connector.AbstractSourceInfo;
import io.debezium.connector.mysql.MySqlOffsetContext;
import io.debezium.data.Envelope;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.schema.DataCollectionId;
import io.debezium.util.Collect;
import java.time.Instant;
import java.util.Map;
import org.apache.kafka.connect.data.Struct;

/** Copied from debezium for accessing here. */
public class MySqlEventMetadataProvider implements EventMetadataProvider {
    public static final String SERVER_ID_KEY = "server_id";

    public static final String GTID_KEY = "gtid";
    public static final String BINLOG_FILENAME_OFFSET_KEY = "file";
    public static final String BINLOG_POSITION_OFFSET_KEY = "pos";
    public static final String BINLOG_ROW_IN_EVENT_OFFSET_KEY = "row";
    public static final String THREAD_KEY = "thread";
    public static final String QUERY_KEY = "query";

    @Override
    public Instant getEventTimestamp(
            DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        if (value == null) {
            return null;
        }
        final Struct sourceInfo = value.getStruct(Envelope.FieldName.SOURCE);
        if (source == null) {
            return null;
        }
        final Long timestamp = sourceInfo.getInt64(AbstractSourceInfo.TIMESTAMP_KEY);
        return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
    }

    @Override
    public Map<String, String> getEventSourcePosition(
            DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        if (value == null) {
            return null;
        }
        final Struct sourceInfo = value.getStruct(Envelope.FieldName.SOURCE);
        if (source == null) {
            return null;
        }
        return Collect.hashMapOf(
                BINLOG_FILENAME_OFFSET_KEY,
                sourceInfo.getString(BINLOG_FILENAME_OFFSET_KEY),
                BINLOG_POSITION_OFFSET_KEY,
                Long.toString(sourceInfo.getInt64(BINLOG_POSITION_OFFSET_KEY)),
                BINLOG_ROW_IN_EVENT_OFFSET_KEY,
                Integer.toString(sourceInfo.getInt32(BINLOG_ROW_IN_EVENT_OFFSET_KEY)));
    }

    @Override
    public String getTransactionId(
            DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        return ((MySqlOffsetContext) offset).getTransactionId();
    }
}
