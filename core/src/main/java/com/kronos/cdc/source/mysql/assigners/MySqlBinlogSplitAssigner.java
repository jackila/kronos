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

import static com.kronos.cdc.source.mysql.debezium.DebeziumUtils.currentBinlogOffset;

import com.kronos.cdc.source.mysql.assigners.state.BinlogPendingSplitsState;
import com.kronos.cdc.source.mysql.assigners.state.PendingSplitsState;
import com.kronos.cdc.source.mysql.debezium.DebeziumUtils;
import com.kronos.cdc.source.mysql.source.config.MySqlSourceConfig;
import com.kronos.cdc.source.mysql.source.offset.BinlogOffset;
import com.kronos.cdc.source.mysql.source.split.FinishedSnapshotSplitInfo;
import com.kronos.cdc.source.mysql.source.split.MySqlBinlogSplit;
import com.kronos.cdc.source.mysql.source.split.MySqlSplit;
import com.kronos.utils.FlinkRuntimeException;
import io.debezium.jdbc.JdbcConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A {@link MySqlSplitAssigner} which only read binlog from current binlog position. */
public class MySqlBinlogSplitAssigner implements MySqlSplitAssigner {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlBinlogSplitAssigner.class);
    private static final String BINLOG_SPLIT_ID = "binlog-split";

    private final MySqlSourceConfig sourceConfig;

    private boolean isBinlogSplitAssigned;

    public MySqlBinlogSplitAssigner(MySqlSourceConfig sourceConfig) {
        this(sourceConfig, false);
    }

    public MySqlBinlogSplitAssigner(
            MySqlSourceConfig sourceConfig, BinlogPendingSplitsState checkpoint) {
        this(sourceConfig, checkpoint.isBinlogSplitAssigned());
    }

    private MySqlBinlogSplitAssigner(
            MySqlSourceConfig sourceConfig, boolean isBinlogSplitAssigned) {
        this.sourceConfig = sourceConfig;
        this.isBinlogSplitAssigned = isBinlogSplitAssigned;
    }

    @Override
    public void open() {}

    @Override
    public Optional<MySqlSplit> getNext() {
        if (isBinlogSplitAssigned) {
            return Optional.empty();
        } else {
            isBinlogSplitAssigned = true;
            return Optional.of(createBinlogSplit());
        }
    }

    @Override
    public boolean waitingForFinishedSplits() {
        return false;
    }

    @Override
    public List<FinishedSnapshotSplitInfo> getFinishedSplitInfos() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void onFinishedSplits(Map<String, BinlogOffset> splitFinishedOffsets) {
        // do nothing
    }

    @Override
    public void addSplits(Collection<MySqlSplit> splits) {
        // we don't store the split, but will re-create binlog split later
        isBinlogSplitAssigned = false;
    }

    @Override
    public PendingSplitsState snapshotState(long checkpointId) {
        return new BinlogPendingSplitsState(isBinlogSplitAssigned);
    }

    @Override
    public void notifyCheckpointComplete(long checkpointId) {
        // nothing to do
    }

    @Override
    public AssignerStatus getAssignerStatus() {
        return AssignerStatus.INITIAL_ASSIGNING_FINISHED;
    }

    @Override
    public void suspend() {}

    @Override
    public void wakeup() {}

    @Override
    public void close() {}

    // ------------------------------------------------------------------------------------------

    private MySqlBinlogSplit createBinlogSplit() {
        try (JdbcConnection jdbc = DebeziumUtils.openJdbcConnection(sourceConfig)) {
            return new MySqlBinlogSplit(
                    BINLOG_SPLIT_ID,
                    currentBinlogOffset(jdbc),
                    BinlogOffset.NO_STOPPING_OFFSET,
                    new ArrayList<>(),
                    new HashMap<>(),
                    0);
        } catch (Exception e) {
            throw new FlinkRuntimeException("Read the binlog offset error", e);
        }
    }
}
