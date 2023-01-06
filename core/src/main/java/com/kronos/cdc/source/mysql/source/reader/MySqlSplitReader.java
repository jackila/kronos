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

import static com.kronos.cdc.source.mysql.debezium.DebeziumUtils.createBinaryClient;
import static com.kronos.cdc.source.mysql.debezium.DebeziumUtils.createMySqlConnection;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.kronos.cdc.source.base.source.reader.RecordsWithSplitIds;
import com.kronos.cdc.source.base.source.reader.splitreader.SplitReader;
import com.kronos.cdc.source.base.source.reader.splitreader.SplitsAddition;
import com.kronos.cdc.source.base.source.reader.splitreader.SplitsChange;
import com.kronos.cdc.source.mysql.debezium.reader.BinlogSplitReader;
import com.kronos.cdc.source.mysql.debezium.reader.DebeziumReader;
import com.kronos.cdc.source.mysql.debezium.reader.SnapshotSplitReader;
import com.kronos.cdc.source.mysql.debezium.task.context.StatefulTaskContext;
import com.kronos.cdc.source.mysql.source.MySqlSource;
import com.kronos.cdc.source.mysql.source.config.MySqlSourceConfig;
import com.kronos.cdc.source.mysql.source.split.MySqlRecords;
import com.kronos.cdc.source.mysql.source.split.MySqlSplit;
import com.kronos.cdc.source.mysql.source.split.SourceRecords;
import io.debezium.connector.mysql.MySqlConnection;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The {@link SplitReader} implementation for the {@link MySqlSource}. */
public class MySqlSplitReader implements SplitReader<SourceRecords, MySqlSplit> {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlSplitReader.class);
    private final Queue<MySqlSplit> splits;
    private final MySqlSourceConfig sourceConfig;
    private final int subtaskId;
    private final MySqlSourceReaderContext context;

    private DebeziumReader<SourceRecords, MySqlSplit> currentReader;
    private String currentSplitId;

    public MySqlSplitReader(
            MySqlSourceConfig sourceConfig, int subtaskId, MySqlSourceReaderContext context) {
        this.sourceConfig = sourceConfig;
        this.subtaskId = subtaskId;
        this.splits = new ArrayDeque<>();
        this.context = context;
    }

    @Override
    public RecordsWithSplitIds<SourceRecords> fetch() throws IOException {

        checkSplitOrStartNext();
        checkNeedStopBinlogReader();

        Iterator<SourceRecords> dataIt;
        try {
            dataIt = currentReader.pollSplitRecords();
        } catch (InterruptedException e) {
            LOG.warn("fetch data failed.", e);
            throw new IOException(e);
        }
        return dataIt == null
                ? finishedSnapshotSplit()
                : MySqlRecords.forRecords(currentSplitId, dataIt);
    }

    private void checkNeedStopBinlogReader() {
        if (currentReader instanceof BinlogSplitReader
                && context.needStopBinlogSplitReader()
                && !currentReader.isFinished()) {
            ((BinlogSplitReader) currentReader).stopBinlogReadTask();
        }
    }

    @Override
    public void handleSplitsChanges(SplitsChange<MySqlSplit> splitsChanges) {
        if (!(splitsChanges instanceof SplitsAddition)) {
            throw new UnsupportedOperationException(
                    String.format(
                            "The SplitChange type of %s is not supported.",
                            splitsChanges.getClass()));
        }

        LOG.debug("Handling split change {}", splitsChanges);
        splits.addAll(splitsChanges.splits());
    }

    @Override
    public void wakeUp() {}

    @Override
    public void close() throws Exception {
        if (currentReader != null) {
            LOG.info(
                    "Close current debezium reader {}",
                    currentReader.getClass().getCanonicalName());
            currentReader.close();
            currentSplitId = null;
        }
    }

    private void checkSplitOrStartNext() throws IOException {
        if (canAssignNextSplit()) {
            MySqlSplit nextSplit = splits.poll();
            if (nextSplit == null) {
                return;
            }

            currentSplitId = nextSplit.splitId();

            if (nextSplit.isSnapshotSplit()) {
                if (currentReader instanceof BinlogSplitReader) {
                    LOG.info(
                            "This is the point from binlog split reading change to snapshot split reading");
                    currentReader.close();
                    currentReader = null;
                }
                if (currentReader == null) {
                    final MySqlConnection jdbcConnection = createMySqlConnection(sourceConfig);
                    final BinaryLogClient binaryLogClient =
                            createBinaryClient(sourceConfig.getDbzConfiguration());
                    final StatefulTaskContext statefulTaskContext =
                            new StatefulTaskContext(sourceConfig, binaryLogClient, jdbcConnection);
                    currentReader = new SnapshotSplitReader(statefulTaskContext, subtaskId);
                }
            } else {
                // point from snapshot split to binlog split
                if (currentReader != null) {
                    LOG.info("It's turn to read binlog split, close current snapshot reader");
                    currentReader.close();
                }
                final MySqlConnection jdbcConnection = createMySqlConnection(sourceConfig);
                final BinaryLogClient binaryLogClient =
                        createBinaryClient(sourceConfig.getDbzConfiguration());
                final StatefulTaskContext statefulTaskContext =
                        new StatefulTaskContext(sourceConfig, binaryLogClient, jdbcConnection);
                currentReader = new BinlogSplitReader(statefulTaskContext, subtaskId);
                LOG.info("BinlogSplitReader is created.");
            }
            currentReader.submitSplit(nextSplit);
        }
    }

    private boolean canAssignNextSplit() {
        return currentReader == null || currentReader.isFinished();
    }

    private MySqlRecords finishedSnapshotSplit() {
        final MySqlRecords finishedRecords = MySqlRecords.forFinishedSplit(currentSplitId);
        currentSplitId = null;
        return finishedRecords;
    }
}
