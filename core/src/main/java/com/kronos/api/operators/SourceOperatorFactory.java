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

package com.kronos.api.operators;

import com.kronos.api.connector.source.SourceSplit;
import com.kronos.jobgraph.physic.operator.StreamOperator;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.physic.operator.source.SourceOperator;
import com.kronos.jobgraph.physic.operator.source.SourceReader;
import com.kronos.jobgraph.physic.operator.source.SourceReaderContext;
import com.kronos.runtime.operators.coordination.OperatorEventGateway;
import com.kronos.utils.FunctionWithException;

/**
 * 根据不同的source、创造source operator，并将source operator注册到dispacher中 通过dispatcher将source
 * operator与coordinate 关联起来
 */
public class SourceOperatorFactory implements StreamOperatorFactory {

    private static final long serialVersionUID = 1L;

    private final Source source;

    public SourceOperatorFactory(Source source) {
        this.source = source;
    }

    public <T extends StreamOperator> T createStreamOperator(StreamOperatorParameters parameters) {
        final OperatorEventGateway gateway =
                parameters
                        .getOperatorEventDispatcher()
                        .getOperatorEventGateway(parameters.getOperatorId());

        final SourceOperator sourceOperator =
                instantiateSourceOperator(source::createSourceReader, gateway);

        // setup
        parameters.getOperatorEventDispatcher().registerEventHandler(0, sourceOperator);

        // today's lunch is generics spaghetti
        @SuppressWarnings("unchecked")
        final T castedOperator = (T) sourceOperator;

        return castedOperator;
    }

    /**
     * This is a utility method to conjure up a "SplitT" generics variable binding so that we can
     * construct the SourceOperator without resorting to "all raw types". That way, this methods
     * puts all "type non-safety" in one place and allows to maintain as much generics safety in the
     * main code as possible.
     */
    private static <T, SplitT extends SourceSplit>
            SourceOperator<T, SplitT> instantiateSourceOperator(
                    FunctionWithException<SourceReaderContext, SourceReader, Exception>
                            readerFactory,
                    OperatorEventGateway eventGateway) {

        return new SourceOperator(readerFactory, eventGateway);
    }
}
