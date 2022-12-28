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

package com.kronos.cdc.source.mysql.source.utils;

import com.kronos.types.RowType;
import io.debezium.relational.Column;
import io.debezium.relational.Table;
import org.kronos.utils.Preconditions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/** Utilities to split chunks of table. */
public class ChunkUtils {

    private ChunkUtils() {}

    public static RowType getChunkKeyColumnType(Table table, String chunkKeyColumn) {
        return getChunkKeyColumnType(getChunkKeyColumn(table, chunkKeyColumn));
    }

    public static RowType getChunkKeyColumnType(Column chunkKeyColumn) {
        return new RowType(chunkKeyColumn.name(),chunkKeyColumn.typeName());
    }

    public static Column getChunkKeyColumn(Table table,  String chunkKeyColumn) {
        List<Column> primaryKeys = table.primaryKeyColumns();
        if (primaryKeys.isEmpty()) {
            throw new ValidationException(
                    String.format(
                            "Incremental snapshot for tables requires primary key,"
                                    + " but table %s doesn't have primary key.",
                            table.id()));
        }

        if (chunkKeyColumn != null) {
            Optional<Column> targetPkColumn =
                    primaryKeys.stream()
                            .filter(col -> chunkKeyColumn.equals(col.name()))
                            .findFirst();
            if (targetPkColumn.isPresent()) {
                return targetPkColumn.get();
            }
            throw new ValidationException(
                    String.format(
                            "Chunk key column '%s' doesn't exist in the primary key [%s] of the table %s.",
                            chunkKeyColumn,
                            primaryKeys.stream().map(Column::name).collect(Collectors.joining(",")),
                            table.id()));
        }

        // use the first column of primary key columns as the chunk key column by default
        return primaryKeys.get(0);
    }

    /** Returns next meta group id according to received meta number and meta group size. */
    public static int getNextMetaGroupId(int receivedMetaNum, int metaGroupSize) {
        Preconditions.checkState(metaGroupSize > 0);
        return receivedMetaNum % metaGroupSize == 0
                ? (receivedMetaNum / metaGroupSize)
                : (receivedMetaNum / metaGroupSize) + 1;
    }
}
