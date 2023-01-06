package com.kronos.api;

import com.kronos.jobgraph.common.ExecutionConfig;

/** */
public class StreamExecutionEnvironment {

    /** The default parallelism used when creating a local environment. */
    private static int defaultLocalParallelism = 1; // Runtime.getRuntime().availableProcessors();

    /** The execution configuration for this environment. */
    private final ExecutionConfig config = new ExecutionConfig();

    /**
     * Sets the parallelism for operations executed through this environment. Setting a parallelism
     * of x here will cause all operators (such as map, batchReduce) to run with x parallel
     * instances. This method overrides the default parallelism for this environment. The {@link
     * LocalStreamEnvironment} uses by default a value equal to the number of hardware contexts (CPU
     * cores / threads). When executing the program via the command line client from a JAR file, the
     * default degree of parallelism is the one configured for that setup.
     *
     * @param parallelism The parallelism
     */
    public StreamExecutionEnvironment setParallelism(int parallelism) {
        config.setParallelism(parallelism);
        return this;
    }
}
