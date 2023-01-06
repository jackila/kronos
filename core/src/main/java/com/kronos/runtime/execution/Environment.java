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

package com.kronos.runtime.execution;

import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.tasks.TaskOperatorEventGateway;

/**
 * The Environment gives the code executed in a task access to the task's properties (such as name,
 * parallelism), the configurations, the data stream readers and writers, as well as the various
 * components that are provided by the TaskManager, such as memory manager, I/O manager, ...
 */
public interface Environment {

    /**
     * Gets the ID of the task execution attempt.
     *
     * @return The ID of the task execution attempt.
     */
    int getExecutionId();

    /** Gets the gateway through which operators can send events to the operator coordinators. */
    TaskOperatorEventGateway getOperatorCoordinatorEventGateway();

    int getOperatorId();

    public Source source();
}
