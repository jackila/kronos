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
import java.util.List;

/**
 * The {@link SourceEvent} that {@link MySqlSourceEnumerator} sends to {@link MySqlSourceReader} to
 * notify the finished snapshot splits has been received, i.e. acknowledge for {@link
 * FinishedSnapshotSplitsReportEvent}.
 */
public class FinishedSnapshotSplitsAckEvent implements SourceEvent {

    private static final long serialVersionUID = 1L;

    private final List<String> finishedSplits;

    public FinishedSnapshotSplitsAckEvent(List<String> finishedSplits) {
        this.finishedSplits = finishedSplits;
    }

    public List<String> getFinishedSplits() {
        return finishedSplits;
    }

    @Override
    public String toString() {
        return "FinishedSnapshotSplitsAckEvent{" + "finishedSplits=" + finishedSplits + '}';
    }
}
