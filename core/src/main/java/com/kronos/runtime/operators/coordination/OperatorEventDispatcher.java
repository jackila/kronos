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


import com.kronos.utils.FlinkException;

/**
 *  从coordiantor 发送数据到该dispatcher，
 *  而该dispacher转发给响应的operator
 *  1. 何时初始化
 *  2. 发送给operator吗？还是响应的线程
 *
 *  operator既可以通过它获取到event，也可以通过它发送event到coordinator
 *
 */
public interface OperatorEventDispatcher {

    void dispatchEventToHandlers(
            int operatorID,
            OperatorEvent evt)
            throws FlinkException;

    /**
     * Register a listener that is notified every time an OperatorEvent is sent from the
     * OperatorCoordinator (of the operator with the given OperatorID) to this subtask.
     */
    void registerEventHandler(int operator ,OperatorEventHandler handler);

    /**
     * Gets the gateway through which events can be passed to the OperatorCoordinator for the
     * operator identified by the given OperatorID.
     */
    OperatorEventGateway getOperatorEventGateway(int operatorId);
}
