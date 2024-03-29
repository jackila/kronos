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

import com.kronos.jobgraph.physic.operator.source.SourceReaderContext;

/**
 * A wrapper class that wraps {@link SourceReaderContext} for sharing message between {@link
 * MySqlSourceReader} and {@link MySqlSplitReader}.
 */
public class MySqlSourceReaderContext {

    private final SourceReaderContext sourceReaderContext;
    private volatile boolean stopBinlogSplitReader;

    public MySqlSourceReaderContext(final SourceReaderContext sourceReaderContext) {
        this.sourceReaderContext = sourceReaderContext;
        this.stopBinlogSplitReader = false;
    }

    public SourceReaderContext getSourceReaderContext() {
        return sourceReaderContext;
    }

    public boolean needStopBinlogSplitReader() {
        return stopBinlogSplitReader;
    }

    public void setStopBinlogSplitReader() {
        this.stopBinlogSplitReader = true;
    }

    public void resetStopBinlogSplitReader() {
        this.stopBinlogSplitReader = false;
    }
}
