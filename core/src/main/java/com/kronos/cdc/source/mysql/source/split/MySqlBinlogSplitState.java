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

package com.kronos.cdc.source.mysql.source.split;

import com.kronos.cdc.source.mysql.source.offset.BinlogOffset;
import io.debezium.relational.TableId;
import io.debezium.relational.history.TableChanges;
import java.util.Map;

/** The state of split to describe the binlog of MySql table(s). */
public class MySqlBinlogSplitState extends MySqlSplitState {

    private BinlogOffset startingOffset;
    private BinlogOffset endingOffset;
    private final Map<TableId, TableChanges.TableChange> tableSchemas;

    public MySqlBinlogSplitState(MySqlBinlogSplit split) {
        super(split);
        this.startingOffset = split.getStartingOffset();
        this.endingOffset = split.getEndingOffset();
        this.tableSchemas = split.getTableSchemas();
    }

    public BinlogOffset getStartingOffset() {
        return startingOffset;
    }

    public void setStartingOffset(BinlogOffset startingOffset) {
        this.startingOffset = startingOffset;
    }

    public BinlogOffset getEndingOffset() {
        return endingOffset;
    }

    public void setEndingOffset(BinlogOffset endingOffset) {
        this.endingOffset = endingOffset;
    }

    public Map<TableId, TableChanges.TableChange> getTableSchemas() {
        return tableSchemas;
    }

    public void recordSchema(TableId tableId, TableChanges.TableChange latestTableChange) {
        this.tableSchemas.put(tableId, latestTableChange);
    }

    public MySqlBinlogSplit toMySqlSplit() {
        final MySqlBinlogSplit binlogSplit = split.asBinlogSplit();
        return new MySqlBinlogSplit(
                binlogSplit.splitId(),
                getStartingOffset(),
                getEndingOffset(),
                binlogSplit.asBinlogSplit().getFinishedSnapshotSplitInfos(),
                getTableSchemas(),
                binlogSplit.getTotalFinishedSplitSize(),
                binlogSplit.isSuspended());
    }

    @Override
    public String toString() {
        return "MySqlBinlogSplitState{"
                + "startingOffset="
                + startingOffset
                + ", endingOffset="
                + endingOffset
                + ", split="
                + split
                + '}';
    }
}
