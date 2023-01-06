/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.api.operators;

import com.kronos.runtime.operators.coordination.OperatorEventDispatcher;
import com.kronos.runtime.tasks.Output;

/** @param <OUT> */
public class StreamOperatorParameters<OUT> {

    private final OperatorEventDispatcher operatorEventDispatcher;
    private final Output output;
    private final int operatorId;

    public StreamOperatorParameters(
            OperatorEventDispatcher operatorEventDispatcher, Output output, int operatorId) {
        this.operatorEventDispatcher = operatorEventDispatcher;
        this.output = output;
        this.operatorId = operatorId;
    }

    public OperatorEventDispatcher getOperatorEventDispatcher() {
        return operatorEventDispatcher;
    }

    public int getOperatorId() {
        return operatorId;
    }
}
