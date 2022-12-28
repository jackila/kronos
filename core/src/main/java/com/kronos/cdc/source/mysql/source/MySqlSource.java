package com.kronos.cdc.source.mysql.source;

import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.api.connector.source.SplitEnumeratorContext;
import com.kronos.cdc.source.base.options.StartupMode;
import com.kronos.cdc.source.base.source.reader.RecordsWithSplitIds;
import com.kronos.cdc.source.base.source.reader.synchronization.FutureCompletingBlockingQueue;
import com.kronos.cdc.source.mysql.MySqlValidator;
import com.kronos.cdc.source.mysql.assigners.MySqlBinlogSplitAssigner;
import com.kronos.cdc.source.mysql.assigners.MySqlHybridSplitAssigner;
import com.kronos.cdc.source.mysql.assigners.MySqlSplitAssigner;
import com.kronos.cdc.source.mysql.debezium.DebeziumUtils;
import com.kronos.cdc.source.mysql.source.config.MySqlSourceConfig;
import com.kronos.cdc.source.mysql.source.config.MySqlSourceConfigFactory;
import com.kronos.cdc.source.mysql.source.enumerator.MySqlSourceEnumerator;
import com.kronos.cdc.source.mysql.source.reader.MySqlRecordEmitter;
import com.kronos.cdc.source.mysql.source.reader.MySqlSourceReader;
import com.kronos.cdc.source.mysql.source.reader.MySqlSourceReaderContext;
import com.kronos.cdc.source.mysql.source.reader.MySqlSplitReader;
import com.kronos.cdc.source.mysql.source.split.SourceRecords;
import com.kronos.cdc.debezium.DebeziumDeserializationSchema;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.physic.operator.source.SourceReader;
import com.kronos.jobgraph.physic.operator.source.SourceReaderContext;
import com.kronos.utils.FlinkRuntimeException;
import io.debezium.jdbc.JdbcConnection;

import java.util.ArrayList;
import java.util.function.Supplier;

import static com.kronos.cdc.source.mysql.debezium.DebeziumUtils.openJdbcConnection;

/**
 * The MySQL CDC Source based on FLIP-27 and Watermark Signal Algorithm which supports parallel
 * reading snapshot of table and then continue to capture data change from binlog.
 *
 * <pre>
 *     1. The source supports parallel capturing table change.
 *     2. The source supports checkpoint in split level when read snapshot data.
 *     3. The source doesn't need apply any lock of MySQL.
 * </pre>
 *
 * <pre>{@code
 * MySqlSource
 *     .<String>builder()
 *     .hostname("localhost")
 *     .port(3306)
 *     .databaseList("mydb")
 *     .tableList("mydb.users")
 *     .username(username)
 *     .password(password)
 *     .serverId(5400)
 *     .deserializer(new JsonDebeziumDeserializationSchema())
 *     .build();
 * }</pre>
 *
 * <p>See {@link MySqlSourceBuilder} for more details.
 *
 * @param <T> the output type of the source.
 */
public class MySqlSource<T> implements Source {
    private static final long serialVersionUID = 1L;

    private final MySqlSourceConfigFactory configFactory;
    private final DebeziumDeserializationSchema<T> deserializationSchema;

    /**
     * Get a MySqlParallelSourceBuilder to build a {@link MySqlSource}.
     *
     * @return a MySql parallel source builder.
     */
    public static <T> MySqlSourceBuilder<T> builder() {
        return new MySqlSourceBuilder<>();
    }

    MySqlSource(
            MySqlSourceConfigFactory configFactory,
            DebeziumDeserializationSchema<T> deserializationSchema) {
        this.configFactory = configFactory;
        this.deserializationSchema = deserializationSchema;
    }


    @Override
    public SourceReader createSourceReader(SourceReaderContext readerContext) {
        // create source config for the given subtask (e.g. unique server id)
        MySqlSourceConfig sourceConfig =
                configFactory.createConfig(readerContext.getIndexOfSubtask());
        FutureCompletingBlockingQueue<RecordsWithSplitIds<SourceRecords>> elementsQueue =
                new FutureCompletingBlockingQueue<>();

        MySqlSourceReaderContext mySqlSourceReaderContext =
                new MySqlSourceReaderContext(readerContext);
        Supplier<MySqlSplitReader> splitReaderSupplier =
                () ->
                        new MySqlSplitReader(
                                sourceConfig,
                                readerContext.getIndexOfSubtask(),
                                mySqlSourceReaderContext);
        return new MySqlSourceReader<>(
                elementsQueue,
                splitReaderSupplier,
                new MySqlRecordEmitter<>(
                        deserializationSchema,
                        sourceConfig.isIncludeSchemaChanges()),
                readerContext.getConfiguration(),
                mySqlSourceReaderContext,
                sourceConfig);
    }

    @Override
    public SplitEnumerator createEnumerator(SplitEnumeratorContext enumContext) {
        MySqlSourceConfig sourceConfig = configFactory.createConfig(0);

        final MySqlValidator validator = new MySqlValidator(sourceConfig);
        validator.validate();

        final MySqlSplitAssigner splitAssigner;
        if (sourceConfig.getStartupOptions().startupMode == StartupMode.INITIAL) {
            try (JdbcConnection jdbc = openJdbcConnection(sourceConfig)) {
                boolean isTableIdCaseSensitive = DebeziumUtils.isTableIdCaseSensitive(jdbc);
                splitAssigner =
                        new MySqlHybridSplitAssigner(
                                sourceConfig,
                                enumContext.currentParallelism(),
                                new ArrayList<>(),
                                isTableIdCaseSensitive);
            } catch (Exception e) {
                throw new FlinkRuntimeException(
                        "Failed to discover captured tables for enumerator", e);
            }
        } else {
            splitAssigner = new MySqlBinlogSplitAssigner(sourceConfig);
        }

        return new MySqlSourceEnumerator(enumContext, sourceConfig, splitAssigner);
    }
}
