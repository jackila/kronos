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

package com.kronos.runtime.source.coordinator;

import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.operators.coordination.SubtaskAccess;
import com.kronos.utils.FlinkRuntimeException;

/**
 * Implementation of the {@link OperatorCoordinator.SubtaskGateway} interface that access to
 * subtasks for status and event sending via {@link SubtaskAccess}.
 */
public class SubtaskGatewayImpl implements OperatorCoordinator.SubtaskGateway {

    private static final String EVENT_LOSS_ERROR_MESSAGE =
            "An OperatorEvent from an OperatorCoordinator to a task was lost. "
                    + "Triggering task failover to ensure consistency. Event: '%s', targetTask: %s";

    private final SubtaskAccess subtaskAccess;

    public SubtaskGatewayImpl(SubtaskAccess subtaskAccess) {
        this.subtaskAccess = subtaskAccess;
    }

    @Override
    public void sendEvent(OperatorEvent evt) throws Exception {
        if (!isReady()) {
            throw new FlinkRuntimeException("SubtaskGateway is not ready, task not yet running.");
        }
        subtaskAccess.createEventSendAction(evt);
    }

    @Override
    public int getSubtask() {
        return subtaskAccess.getSubtaskIndex();
    }

    private boolean isReady() {
        return true;
    }
}
