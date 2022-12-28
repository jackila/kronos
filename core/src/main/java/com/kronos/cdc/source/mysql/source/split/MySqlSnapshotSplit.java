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
import com.kronos.types.RowType;
import io.debezium.relational.TableId;
import io.debezium.relational.history.TableChanges.TableChange;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/** The split to describe a split of a MySql table snapshot. */
public class MySqlSnapshotSplit extends MySqlSplit {

    private final TableId tableId;
    private final RowType splitKeyType;
    private final Map<TableId, TableChange> tableSchemas;

     private final Object[] splitStart;
     private final Object[] splitEnd;
    /** The high watermark is not null when the split read finished. */
     private final BinlogOffset highWatermark;

     transient byte[] serializedFormCache;

    public MySqlSnapshotSplit(
            TableId tableId,
            String splitId,
            RowType splitKeyType,
            Object[] splitStart,
            Object[] splitEnd,
            BinlogOffset highWatermark,
            Map<TableId, TableChange> tableSchemas) {
        super(splitId);
        this.tableId = tableId;
        this.splitKeyType = splitKeyType;
        this.splitStart = splitStart;
        this.splitEnd = splitEnd;
        this.highWatermark = highWatermark;
        this.tableSchemas = tableSchemas;
    }

    public TableId getTableId() {
        return tableId;
    }

    
    public Object[] getSplitStart() {
        return splitStart;
    }

    
    public Object[] getSplitEnd() {
        return splitEnd;
    }

    
    public BinlogOffset getHighWatermark() {
        return highWatermark;
    }

    public boolean isSnapshotReadFinished() {
        return highWatermark != null;
    }

    @Override
    public Map<TableId, TableChange> getTableSchemas() {
        return tableSchemas;
    }

    /** Casts this split into a {@link MySqlSchemalessSnapshotSplit}. */
    public final MySqlSchemalessSnapshotSplit toSchemalessSnapshotSplit() {
        return new MySqlSchemalessSnapshotSplit(
                tableId, splitId, splitKeyType, splitStart, splitEnd, highWatermark);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MySqlSnapshotSplit that = (MySqlSnapshotSplit) o;
        return Objects.equals(tableId, that.tableId)
                && Objects.equals(splitKeyType, that.splitKeyType)
                && Arrays.equals(splitStart, that.splitStart)
                && Arrays.equals(splitEnd, that.splitEnd)
                && Objects.equals(highWatermark, that.highWatermark);
    }

    public RowType getSplitKeyType() {
        return splitKeyType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), tableId, splitKeyType, highWatermark);
        result = 31 * result + Arrays.hashCode(splitStart);
        result = 31 * result + Arrays.hashCode(splitEnd);
        result = 31 * result + Arrays.hashCode(serializedFormCache);
        return result;
    }

    @Override
    public String toString() {
        String splitKeyTypeSummary = "[" + splitKeyType.getName() + "]";

        return "MySqlSnapshotSplit{"
                + "tableId="
                + tableId
                + ", splitId='"
                + splitId
                + '\''
                + ", splitKeyType="
                + splitKeyTypeSummary
                + ", splitStart="
                + Arrays.toString(splitStart)
                + ", splitEnd="
                + Arrays.toString(splitEnd)
                + ", highWatermark="
                + highWatermark
                + '}';
    }
}
