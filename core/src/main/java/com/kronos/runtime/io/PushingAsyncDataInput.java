/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.runtime.io;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.runtime.tasks.Output;

/** @param <T> */
public interface PushingAsyncDataInput<T> extends AvailabilityProvider {

    /**
     * Pushes the next element to the output from current data input, and returns the input status
     * to indicate whether there are more available data in current input.
     *
     * <p>This method should be non blocking.
     */
    DataInputStatus emitNext(Output output) throws Exception;

    /**
     * Basic data output interface used in emitting the next element from data input.
     *
     * <p>The type encapsulated with the stream record.
     */
    interface DataOutput {

        void emitRecord(StreamRecord streamRecord) throws Exception;
    }
}
