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

import com.kronos.runtime.executiongraph.ExecutionGraph;
import com.kronos.runtime.operators.coordination.OperatorCoordinatorHolder;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.source.coordinator.OperatorCoordinator;
import com.kronos.utils.FlinkException;
import com.kronos.utils.FlinkRuntimeException;
import com.kronos.utils.IOUtils;
import org.kronos.utils.ExceptionUtils;

/**
 * Default handler for the {@link OperatorCoordinator OperatorCoordinators}.
 */
public class DefaultOperatorCoordinatorHandler implements OperatorCoordinatorHandler {

    private final OperatorCoordinatorHolder coordinatorHolder;


    public DefaultOperatorCoordinatorHandler(
            ExecutionGraph executionGraph) {
        this.coordinatorHolder = createCoordinatorMap(executionGraph);
    }

    private static OperatorCoordinatorHolder createCoordinatorMap(
            ExecutionGraph executionGraph) {
        return executionGraph.coordinatorHolder();
    }

    @Override
    public void initializeOperatorCoordinators() {
        coordinatorHolder.lazyInitialize();
    }

    @Override
    public void startAllOperatorCoordinators() {
        try {
            coordinatorHolder.start();
        } catch (Throwable t) {
            ExceptionUtils.rethrowIfFatalErrorOrOOM(t);
            IOUtils.closeQuietly(coordinatorHolder);
            throw new FlinkRuntimeException("Failed to start the operator coordinators", t);
        }
    }

    @Override
    public void disposeAllOperatorCoordinators() {
        IOUtils.closeQuietly(coordinatorHolder);
    }

    @Override
    public void deliverOperatorEventToCoordinator(
            final int taskExecutionId,
            final int operatorId,
            final OperatorEvent evt)
            throws FlinkException {

        // Failure semantics (as per the javadocs of the method):
        // If the task manager sends an event for a non-running task or an non-existing operator
        // coordinator, then respond with an exception to the call. If task and coordinator exist,
        // then we assume that the call from the TaskManager was valid, and any bubbling exception
        // needs to cause a job failure.
        try {
            coordinatorHolder.handleEventFromOperator(taskExecutionId, evt);
        } catch (Throwable t) {
            ExceptionUtils.rethrowIfFatalErrorOrOOM(t);
        }
    }

}
