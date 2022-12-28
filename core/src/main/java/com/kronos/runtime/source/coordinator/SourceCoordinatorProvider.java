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

package com.kronos.runtime.source.coordinator;

import com.kronos.api.connector.source.SourceSplit;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.operators.coordination.RecreateOnResetOperatorCoordinator;
import com.kronos.utils.FatalExitExceptionHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;

/** The provider of {@link SourceCoordinator}. */
public class SourceCoordinatorProvider<SplitT extends SourceSplit> extends RecreateOnResetOperatorCoordinator.Provider{
    private static final long serialVersionUID = -1921681440009738462L;
    private final String operatorName;
    private final Source source;
    //todo
    private final int numWorkerThreads = 1;

    /**
     * Construct the {@link SourceCoordinatorProvider}.
     *
     * @param operatorName the name of the operator.
     * @param operatorID the ID of the operator this coordinator corresponds to.
     * @param source the Source that will be used for this coordinator.
     * @param numWorkerThreads the number of threads the should provide to the SplitEnumerator for
     *     doing async calls. See {@link
     *     org.apache.flink.api.connector.source.SplitEnumeratorContext#callAsync(Callable,
     *     BiConsumer) SplitEnumeratorContext.callAsync()}.
     */
    public SourceCoordinatorProvider(
            String operatorName,
            int operatorID,
            Source source) {
        super(operatorID);
        this.operatorName = operatorName;
        this.source = source;
    }

    public OperatorCoordinator getCoordinator(OperatorCoordinator.Context context) {
        final String coordinatorThreadName = "SourceCoordinator-" + operatorName;
        CoordinatorExecutorThreadFactory coordinatorThreadFactory =
                new CoordinatorExecutorThreadFactory(
                        coordinatorThreadName);
        ExecutorService coordinatorExecutor =
                Executors.newSingleThreadExecutor(coordinatorThreadFactory);

        SourceCoordinatorContext<SplitT> sourceCoordinatorContext =
                new SourceCoordinatorContext<>(
                        coordinatorExecutor,
                        coordinatorThreadFactory,
                        numWorkerThreads,
                        context
                        );
        return new SourceCoordinator(
                operatorName, coordinatorExecutor, source, sourceCoordinatorContext);
    }

    /** A thread factory class that provides some helper methods. */
    public static class CoordinatorExecutorThreadFactory
            implements ThreadFactory, Thread.UncaughtExceptionHandler {

        private final String coordinatorThreadName;
        private final Thread.UncaughtExceptionHandler errorHandler;

        private Thread t;

        CoordinatorExecutorThreadFactory(
                final String coordinatorThreadName) {
            this(coordinatorThreadName, FatalExitExceptionHandler.INSTANCE);
        }

        CoordinatorExecutorThreadFactory(
                final String coordinatorThreadName,
                final Thread.UncaughtExceptionHandler errorHandler) {
            this.coordinatorThreadName = coordinatorThreadName;
            this.errorHandler = errorHandler;
        }

        @Override
        public synchronized Thread newThread(Runnable r) {
            t = new Thread(r, coordinatorThreadName);
            t.setUncaughtExceptionHandler(this);
            return t;
        }

        @Override
        public synchronized void uncaughtException(Thread t, Throwable e) {
            errorHandler.uncaughtException(t, e);
        }

        String getCoordinatorThreadName() {
            return coordinatorThreadName;
        }

        boolean isCurrentThreadCoordinatorThread() {
            return Thread.currentThread() == t;
        }
    }
}
