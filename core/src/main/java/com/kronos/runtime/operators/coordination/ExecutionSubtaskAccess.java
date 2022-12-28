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

import com.kronos.runtime.executiongraph.Execution;
import com.kronos.runtime.executiongraph.ExecutionJobVertex;
import com.kronos.utils.FlinkException;

import static org.kronos.utils.Preconditions.checkNotNull;


/**
 * An implementation of the {@link SubtaskAccess} interface that uses the ExecutionGraph's classes,
 * specifically {@link Execution} and {@link ExecutionJobVertex} to access tasks.
 */
final class ExecutionSubtaskAccess implements SubtaskAccess {

    private final Execution taskExecution;
    private final int operator;

    ExecutionSubtaskAccess(Execution taskExecution,
                           int operator) {
        this.taskExecution = taskExecution;
        this.operator = operator;
    }

    @Override
    public void createEventSendAction(
            OperatorEvent event) {

        try {
            taskExecution.sendOperatorEvent(event);
        } catch (FlinkException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSubtaskIndex() {
        return taskExecution.getSubTaskIndex();
    }

    // ------------------------------------------------------------------------

    static final class ExecutionJobVertexSubtaskAccess implements SubtaskAccessFactory {

        private final ExecutionJobVertex ejv;
        private final int operator;

        ExecutionJobVertexSubtaskAccess(ExecutionJobVertex ejv,
                                        int operator) {
            this.ejv = checkNotNull(ejv);
            this.operator = checkNotNull(operator);
        }

        @Override
        public SubtaskAccess getAccessForSubtask(int subtask) {
            if (subtask < 0 || subtask >= ejv.getParallelism()) {
                throw new IllegalArgumentException(
                        "Subtask index out of bounds [0, " + ejv.getParallelism() + ')');
            }

            final Execution taskExecution =
                    ejv.getTaskExecutions().get(subtask);
            return new ExecutionSubtaskAccess(taskExecution, operator);
        }
    }
}
