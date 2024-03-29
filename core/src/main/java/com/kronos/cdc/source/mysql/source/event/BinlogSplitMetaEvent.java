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

package com.kronos.cdc.source.mysql.source.event;

import com.kronos.api.connector.source.SourceEvent;
import com.kronos.cdc.source.mysql.source.enumerator.MySqlSourceEnumerator;
import com.kronos.cdc.source.mysql.source.reader.MySqlSourceReader;
import com.kronos.cdc.source.mysql.source.split.FinishedSnapshotSplitInfo;
import java.util.List;

/**
 * The {@link SourceEvent} that {@link MySqlSourceEnumerator} sends to {@link MySqlSourceReader} to
 * pass binlog meta data, i.e. {@link FinishedSnapshotSplitInfo}.
 */
public class BinlogSplitMetaEvent implements SourceEvent {

    private static final long serialVersionUID = 1L;

    private final String splitId;

    /** The meta data of binlog split is divided to multiple groups. */
    private final int metaGroupId;
    /**
     * The serialized meta data of binlog split, it's serialized/deserialize by {@link
     * FinishedSnapshotSplitInfo#serialize(FinishedSnapshotSplitInfo)} and {@link
     * FinishedSnapshotSplitInfo#deserialize(byte[])}.
     */
    private final List<byte[]> metaGroup;

    public BinlogSplitMetaEvent(String splitId, int metaGroupId, List<byte[]> metaGroup) {
        this.splitId = splitId;
        this.metaGroupId = metaGroupId;
        this.metaGroup = metaGroup;
    }

    public String getSplitId() {
        return splitId;
    }

    public int getMetaGroupId() {
        return metaGroupId;
    }

    public List<byte[]> getMetaGroup() {
        return metaGroup;
    }
}
