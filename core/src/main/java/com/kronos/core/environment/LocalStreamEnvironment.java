package com.kronos.core.environment;

import com.kronos.core.graph.StreamGraph;

/**
 * @Author: jackila
 * @Date: 3:42 PM 2022-6-26
 */
public class LocalStreamEnvironment extends StreamExecutionEnvironment {

    @Override
    public void execute(StreamGraph streamGraph) throws Exception {
        super.execute(streamGraph);
    }
}
