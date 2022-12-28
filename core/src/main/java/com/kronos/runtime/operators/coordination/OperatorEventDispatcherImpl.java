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

package com.kronos.runtime.operators.coordination;

import com.kronos.jobgraph.tasks.TaskOperatorEventGateway;
import com.kronos.utils.FlinkException;

import java.util.HashMap;
import java.util.Map;

import static org.kronos.utils.Preconditions.checkNotNull;


/**
 * An implementation of the {@link OperatorEventDispatcher}.
 *
 * <p>This class is intended for single threaded use from the stream task mailbox.
 */
public final class OperatorEventDispatcherImpl implements OperatorEventDispatcher {

    private final Map<Integer, OperatorEventHandler> handlers;

    private final TaskOperatorEventGateway toCoordinator;

    public OperatorEventDispatcherImpl(
            TaskOperatorEventGateway toCoordinator) {
        this.toCoordinator = checkNotNull(toCoordinator);
        this.handlers = new HashMap<>();
    }

    @Override
    public void dispatchEventToHandlers(
            int operatorID,
            OperatorEvent evt)
            throws FlinkException {

        final OperatorEventHandler handler = handlers.get(operatorID);
        if (handler != null) {
            handler.handleOperatorEvent(evt);
        } else {
            throw new FlinkException("Operator not registered for operator events");
        }
    }

    @Override
    public void registerEventHandler(int operator, OperatorEventHandler handler) {
        final OperatorEventHandler prior = handlers.putIfAbsent(operator, handler);
        if (prior != null) {
            throw new IllegalStateException("already a handler registered for this operatorId");
        }
    }

    @Override
    public OperatorEventGateway getOperatorEventGateway(int operatorId) {
        return new OperatorEventGatewayImpl(toCoordinator, operatorId);
    }

    // ------------------------------------------------------------------------

    private static final class OperatorEventGatewayImpl implements OperatorEventGateway {

        private final TaskOperatorEventGateway toCoordinator;

        private final int operatorId;

        private OperatorEventGatewayImpl(
                TaskOperatorEventGateway toCoordinator, int operatorId) {
            this.toCoordinator = toCoordinator;
            this.operatorId = operatorId;
        }

        @Override
        public void sendEventToCoordinator(OperatorEvent event) {
            toCoordinator.sendOperatorEventToCoordinator(operatorId, event);
        }
    }
}
