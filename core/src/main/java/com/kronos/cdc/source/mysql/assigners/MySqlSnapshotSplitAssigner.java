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

package com.kronos.cdc.source.mysql.assigners;

import static com.kronos.cdc.source.mysql.assigners.AssignerStatus.isAssigningFinished;
import static com.kronos.cdc.source.mysql.assigners.AssignerStatus.isSuspended;
import static com.kronos.cdc.source.mysql.debezium.DebeziumUtils.discoverCapturedTables;
import static com.kronos.cdc.source.mysql.debezium.DebeziumUtils.openJdbcConnection;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kronos.cdc.source.mysql.assigners.state.SnapshotPendingSplitsState;
import com.kronos.cdc.source.mysql.debezium.DebeziumUtils;
import com.kronos.cdc.source.mysql.schema.MySqlSchema;
import com.kronos.cdc.source.mysql.source.config.MySqlSourceConfig;
import com.kronos.cdc.source.mysql.source.config.MySqlSourceOptions;
import com.kronos.cdc.source.mysql.source.offset.BinlogOffset;
import com.kronos.cdc.source.mysql.source.split.FinishedSnapshotSplitInfo;
import com.kronos.cdc.source.mysql.source.split.MySqlSchemalessSnapshotSplit;
import com.kronos.cdc.source.mysql.source.split.MySqlSnapshotSplit;
import com.kronos.cdc.source.mysql.source.split.MySqlSplit;
import com.kronos.utils.FlinkRuntimeException;
import io.debezium.jdbc.JdbcConnection;
import io.debezium.relational.TableId;
import io.debezium.relational.history.TableChanges;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.kronos.utils.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MySqlSplitAssigner} that splits tables into small chunk splits based on primary key
 * range and chunk size.
 *
 * @see MySqlSourceOptions#SCAN_INCREMENTAL_SNAPSHOT_CHUNK_SIZE
 */
public class MySqlSnapshotSplitAssigner implements MySqlSplitAssigner {
    private static final Logger LOG = LoggerFactory.getLogger(MySqlSnapshotSplitAssigner.class);

    private final List<TableId> alreadyProcessedTables;
    private final List<MySqlSchemalessSnapshotSplit> remainingSplits;
    private final Map<String, MySqlSchemalessSnapshotSplit> assignedSplits;
    private final Map<TableId, TableChanges.TableChange> tableSchemas;
    private final Map<String, BinlogOffset> splitFinishedOffsets;
    private final MySqlSourceConfig sourceConfig;
    private final int currentParallelism;
    private final List<TableId> remainingTables;
    private final boolean isRemainingTablesCheckpointed;
    private final Object lock = new Object();

    private volatile Throwable uncaughtSplitterException;
    private AssignerStatus assignerStatus;
    private ChunkSplitter chunkSplitter;
    private boolean isTableIdCaseSensitive;
    private ExecutorService executor;

    @Nullable private Long checkpointIdToFinish;

    public MySqlSnapshotSplitAssigner(
            MySqlSourceConfig sourceConfig,
            int currentParallelism,
            List<TableId> remainingTables,
            boolean isTableIdCaseSensitive) {
        this(
                sourceConfig,
                currentParallelism,
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                AssignerStatus.INITIAL_ASSIGNING,
                remainingTables,
                isTableIdCaseSensitive,
                true);
    }

    public MySqlSnapshotSplitAssigner(
            MySqlSourceConfig sourceConfig,
            int currentParallelism,
            SnapshotPendingSplitsState checkpoint) {
        this(
                sourceConfig,
                currentParallelism,
                checkpoint.getAlreadyProcessedTables(),
                checkpoint.getRemainingSplits(),
                checkpoint.getAssignedSplits(),
                checkpoint.getTableSchemas(),
                checkpoint.getSplitFinishedOffsets(),
                checkpoint.getSnapshotAssignerStatus(),
                checkpoint.getRemainingTables(),
                checkpoint.isTableIdCaseSensitive(),
                checkpoint.isRemainingTablesCheckpointed());
    }

    private MySqlSnapshotSplitAssigner(
            MySqlSourceConfig sourceConfig,
            int currentParallelism,
            List<TableId> alreadyProcessedTables,
            List<MySqlSchemalessSnapshotSplit> remainingSplits,
            Map<String, MySqlSchemalessSnapshotSplit> assignedSplits,
            Map<TableId, TableChanges.TableChange> tableSchemas,
            Map<String, BinlogOffset> splitFinishedOffsets,
            AssignerStatus assignerStatus,
            List<TableId> remainingTables,
            boolean isTableIdCaseSensitive,
            boolean isRemainingTablesCheckpointed) {
        this.sourceConfig = sourceConfig;
        this.currentParallelism = currentParallelism;
        this.alreadyProcessedTables = alreadyProcessedTables;
        this.remainingSplits = new CopyOnWriteArrayList<>(remainingSplits);
        this.assignedSplits = assignedSplits;
        this.tableSchemas = tableSchemas;
        this.splitFinishedOffsets = splitFinishedOffsets;
        this.assignerStatus = assignerStatus;
        this.remainingTables = new CopyOnWriteArrayList<>(remainingTables);
        this.isRemainingTablesCheckpointed = isRemainingTablesCheckpointed;
        this.isTableIdCaseSensitive = isTableIdCaseSensitive;
    }

    @Override
    public void open() {
        chunkSplitter = createChunkSplitter(sourceConfig, isTableIdCaseSensitive);
        discoveryCaptureTables();
        captureNewlyAddedTables();
        startAsynchronouslySplit();
    }

    private void discoveryCaptureTables() {
        // discovery the tables lazily
        if (needToDiscoveryTables()) {
            long start = System.currentTimeMillis();
            LOG.debug("The remainingTables is empty, start to discovery tables");
            try (JdbcConnection jdbc = openJdbcConnection(sourceConfig)) {
                final List<TableId> discoverTables = discoverCapturedTables(jdbc, sourceConfig);
                this.remainingTables.addAll(discoverTables);
                this.isTableIdCaseSensitive = DebeziumUtils.isTableIdCaseSensitive(jdbc);
            } catch (Exception e) {
                throw new FlinkRuntimeException("Failed to discovery tables to capture", e);
            }
            LOG.debug(
                    "Discovery tables success, time cost: {} ms.",
                    System.currentTimeMillis() - start);
        }
        // when restore the job from legacy savepoint, the legacy state may haven't snapshot
        // remaining tables, discovery remaining table here
        else if (!isRemainingTablesCheckpointed && !isAssigningFinished(assignerStatus)) {
            try (JdbcConnection jdbc = openJdbcConnection(sourceConfig)) {
                final List<TableId> discoverTables = discoverCapturedTables(jdbc, sourceConfig);
                discoverTables.removeAll(alreadyProcessedTables);
                this.remainingTables.addAll(discoverTables);
                this.isTableIdCaseSensitive = DebeziumUtils.isTableIdCaseSensitive(jdbc);
            } catch (Exception e) {
                throw new FlinkRuntimeException(
                        "Failed to discover remaining tables to capture", e);
            }
        }
    }

    private void captureNewlyAddedTables() {
        if (sourceConfig.isScanNewlyAddedTableEnabled()) {
            // check whether we got newly added tables
            try (JdbcConnection jdbc = openJdbcConnection(sourceConfig)) {
                final List<TableId> newlyAddedTables = discoverCapturedTables(jdbc, sourceConfig);
                newlyAddedTables.removeAll(alreadyProcessedTables);
                newlyAddedTables.removeAll(remainingTables);
                if (!newlyAddedTables.isEmpty()) {
                    // if job is still in snapshot reading phase, directly add all newly added
                    // tables
                    LOG.info("Found newly added tables, start capture newly added tables process");
                    remainingTables.addAll(newlyAddedTables);
                    if (isAssigningFinished(assignerStatus)) {
                        // start the newly added tables process under binlog reading phase
                        LOG.info(
                                "Found newly added tables, start capture newly added tables process under binlog reading phase");
                        this.suspend();
                    }
                }
            } catch (Exception e) {
                throw new FlinkRuntimeException(
                        "Failed to discover remaining tables to capture", e);
            }
        }
    }

    private void startAsynchronouslySplit() {
        if (!remainingTables.isEmpty()) {
            if (executor == null) {
                ThreadFactory threadFactory =
                        new ThreadFactoryBuilder().setNameFormat("snapshot-splitting").build();
                this.executor = Executors.newSingleThreadExecutor(threadFactory);
            }

            executor.submit(this::splitChunksForRemainingTables);
        }
    }

    @Override
    public Optional<MySqlSplit> getNext() {
        checkSplitterErrors();
        waitTableDiscoveryReady();
        synchronized (lock) {
            if (!remainingSplits.isEmpty()) {
                // return remaining splits firstly
                Iterator<MySqlSchemalessSnapshotSplit> iterator = remainingSplits.iterator();
                MySqlSchemalessSnapshotSplit split = iterator.next();
                remainingSplits.remove(split);
                assignedSplits.put(split.splitId(), split);
                addAlreadyProcessedTablesIfNotExists(split.getTableId());
                return Optional.of(
                        split.toMySqlSnapshotSplit(tableSchemas.get(split.getTableId())));
            } else if (!remainingTables.isEmpty()) {
                try {
                    // wait for the asynchronous split to complete
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new FlinkRuntimeException(
                            "InterruptedException while waiting for asynchronously snapshot split");
                }
                return getNext();
            } else {
                closeExecutorService();
                return Optional.empty();
            }
        }
    }

    @Override
    public boolean waitingForFinishedSplits() {
        return !allSplitsFinished();
    }

    @Override
    public List<FinishedSnapshotSplitInfo> getFinishedSplitInfos() {
        if (waitingForFinishedSplits()) {
            LOG.error(
                    "The assigner is not ready to offer finished split information, this should not be called");
            throw new FlinkRuntimeException(
                    "The assigner is not ready to offer finished split information, this should not be called");
        }
        final List<MySqlSchemalessSnapshotSplit> assignedSnapshotSplit =
                assignedSplits.values().stream()
                        .sorted(Comparator.comparing(MySqlSplit::splitId))
                        .collect(Collectors.toList());
        List<FinishedSnapshotSplitInfo> finishedSnapshotSplitInfos = new ArrayList<>();
        for (MySqlSchemalessSnapshotSplit split : assignedSnapshotSplit) {
            BinlogOffset binlogOffset = splitFinishedOffsets.get(split.splitId());
            finishedSnapshotSplitInfos.add(
                    new FinishedSnapshotSplitInfo(
                            split.getTableId(),
                            split.splitId(),
                            split.getSplitStart(),
                            split.getSplitEnd(),
                            binlogOffset));
        }
        return finishedSnapshotSplitInfos;
    }

    @Override
    public void onFinishedSplits(Map<String, BinlogOffset> splitFinishedOffsets) {
        this.splitFinishedOffsets.putAll(splitFinishedOffsets);
        if (allSplitsFinished() && AssignerStatus.isAssigning(assignerStatus)) {
            // Skip the waiting checkpoint when current parallelism is 1 which means we do not need
            // to care about the global output data order of snapshot splits and binlog split.
            if (currentParallelism == 1) {
                assignerStatus = assignerStatus.onFinish();
                LOG.info(
                        "Snapshot split assigner received all splits finished and the job parallelism is 1, snapshot split assigner is turn into finished status.");
            } else {
                LOG.info(
                        "Snapshot split assigner received all splits finished, waiting for a complete checkpoint to mark the assigner finished.");
            }
        }
    }

    @Override
    public void addSplits(Collection<MySqlSplit> splits) {
        for (MySqlSplit split : splits) {
            tableSchemas.putAll(split.asSnapshotSplit().getTableSchemas());
            remainingSplits.add(split.asSnapshotSplit().toSchemalessSnapshotSplit());
            // we should remove the add-backed splits from the assigned list,
            // because they are failed
            assignedSplits.remove(split.splitId());
            splitFinishedOffsets.remove(split.splitId());
        }
    }

    @Override
    public SnapshotPendingSplitsState snapshotState(long checkpointId) {
        SnapshotPendingSplitsState state =
                new SnapshotPendingSplitsState(
                        alreadyProcessedTables,
                        remainingSplits,
                        assignedSplits,
                        tableSchemas,
                        splitFinishedOffsets,
                        assignerStatus,
                        remainingTables,
                        isTableIdCaseSensitive,
                        true);
        // we need a complete checkpoint before mark this assigner to be finished, to wait for all
        // records of snapshot splits are completely processed
        if (checkpointIdToFinish == null
                && !isAssigningFinished(assignerStatus)
                && allSplitsFinished()) {
            checkpointIdToFinish = checkpointId;
        }
        return state;
    }

    @Override
    public void notifyCheckpointComplete(long checkpointId) {
        // we have waited for at-least one complete checkpoint after all snapshot-splits are
        // finished, then we can mark snapshot assigner as finished.
        if (checkpointIdToFinish != null
                && !isAssigningFinished(assignerStatus)
                && allSplitsFinished()) {
            if (checkpointId >= checkpointIdToFinish) {
                assignerStatus = assignerStatus.onFinish();
            }
            LOG.info("Snapshot split assigner is turn into finished status.");
        }
    }

    @Override
    public AssignerStatus getAssignerStatus() {
        return assignerStatus;
    }

    @Override
    public void suspend() {
        Preconditions.checkState(
                isAssigningFinished(assignerStatus), "Invalid assigner status {}", assignerStatus);
        assignerStatus = assignerStatus.suspend();
    }

    @Override
    public void wakeup() {
        Preconditions.checkState(
                isSuspended(assignerStatus), "Invalid assigner status {}", assignerStatus);
        assignerStatus = assignerStatus.wakeup();
    }

    @Override
    public void close() {
        closeExecutorService();
    }

    private void closeExecutorService() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void addAlreadyProcessedTablesIfNotExists(TableId tableId) {
        if (!alreadyProcessedTables.contains(tableId)) {
            alreadyProcessedTables.add(tableId);
        }
    }

    private void waitTableDiscoveryReady() {
        while (needToDiscoveryTables()) {
            LOG.debug("Current assigner is discovering tables, wait tables ready...");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
    }

    /** Indicates there is no more splits available in this assigner. */
    public boolean noMoreSplits() {
        return !needToDiscoveryTables() && remainingTables.isEmpty() && remainingSplits.isEmpty();
    }

    /** Indicates current assigner need to discovery tables or not. */
    public boolean needToDiscoveryTables() {
        return remainingTables.isEmpty()
                && remainingSplits.isEmpty()
                && alreadyProcessedTables.isEmpty();
    }

    public Map<String, MySqlSchemalessSnapshotSplit> getAssignedSplits() {
        return assignedSplits;
    }

    public Map<TableId, TableChanges.TableChange> getTableSchemas() {
        return tableSchemas;
    }

    public Map<String, BinlogOffset> getSplitFinishedOffsets() {
        return splitFinishedOffsets;
    }

    // -------------------------------------------------------------------------------------------

    /**
     * Returns whether all splits are finished which means no more splits and all assigned splits
     * are finished.
     */
    private boolean allSplitsFinished() {
        return noMoreSplits() && assignedSplits.size() == splitFinishedOffsets.size();
    }

    private void splitChunksForRemainingTables() {
        try {
            for (TableId nextTable : remainingTables) {
                // split the given table into chunks (snapshot splits)
                Collection<MySqlSnapshotSplit> splits = chunkSplitter.generateSplits(nextTable);

                final Map<TableId, TableChanges.TableChange> tableSchema = new HashMap<>();
                if (!splits.isEmpty()) {
                    tableSchema.putAll(splits.iterator().next().getTableSchemas());
                }
                final List<MySqlSchemalessSnapshotSplit> schemalessSnapshotSplits =
                        splits.stream()
                                .map(MySqlSnapshotSplit::toSchemalessSnapshotSplit)
                                .collect(Collectors.toList());
                synchronized (lock) {
                    tableSchemas.putAll(tableSchema);
                    remainingSplits.addAll(schemalessSnapshotSplits);
                    remainingTables.remove(nextTable);
                    lock.notify();
                }
            }
        } catch (Exception e) {
            if (uncaughtSplitterException == null) {
                uncaughtSplitterException = e;
            } else {
                uncaughtSplitterException.addSuppressed(e);
            }
            // Release the potential waiting getNext() call
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    private void checkSplitterErrors() {
        if (uncaughtSplitterException != null) {
            throw new FlinkRuntimeException(
                    "Chunk splitting has encountered exception", uncaughtSplitterException);
        }
    }

    private static ChunkSplitter createChunkSplitter(
            MySqlSourceConfig sourceConfig, boolean isTableIdCaseSensitive) {
        MySqlSchema mySqlSchema = new MySqlSchema(sourceConfig, isTableIdCaseSensitive);
        return new ChunkSplitter(mySqlSchema, sourceConfig);
    }
}
