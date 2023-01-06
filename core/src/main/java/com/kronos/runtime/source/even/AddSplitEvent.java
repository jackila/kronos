/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.kronos.runtime.source.even;

import com.kronos.api.connector.source.SourceSplit;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A source event that adds splits to a source reader.
 *
 * @param <SplitT> the type of splits.
 */
public class AddSplitEvent<SplitT extends SourceSplit> implements OperatorEvent {

    private static final long serialVersionUID = 1L;
    private final ArrayList<SplitT> splits;

    public AddSplitEvent(List<SplitT> splits) {
        this.splits = new ArrayList<>(splits.size());
        for (SplitT split : splits) {
            this.splits.add(split);
        }
    }

    public List<SplitT> splits() {
        return splits;
    }

    @Override
    public String toString() {
        return String.format("AddSplitEvents[%s]", splits);
    }
}
