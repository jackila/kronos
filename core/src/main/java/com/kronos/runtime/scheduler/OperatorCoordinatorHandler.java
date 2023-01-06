/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.runtime.scheduler;

import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.source.coordinator.OperatorCoordinator;
import com.kronos.utils.FlinkException;

/** Handler for the {@link OperatorCoordinator OperatorCoordinators}. */
public interface OperatorCoordinatorHandler {

    /** Initialize operator coordinators. */
    void initializeOperatorCoordinators();

    /** Start all operator coordinators. */
    void startAllOperatorCoordinators();

    /** Dispose all operator coordinators. */
    void disposeAllOperatorCoordinators();

    /**
     * Delivers an OperatorEvent to a {@link OperatorCoordinator}.
     *
     * @param taskExecutionId Execution attempt id of the originating task.
     * @param operatorId OperatorId of the target OperatorCoordinator.
     * @param event Event to deliver to the OperatorCoordinator.
     * @throws FlinkException If no coordinator is registered for operator.
     */
    void deliverOperatorEventToCoordinator(int taskExecutionId, int operatorId, OperatorEvent event)
            throws FlinkException;
}
