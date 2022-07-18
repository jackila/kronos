package com.kronos.core.graph;

import com.kronos.config.common.ExecutionConfig;

import java.util.Map;
import java.util.Set;

/**
 * @Author: jackila
 * @Date: 2:17 PM 2022-6-18
 */
public class StreamGraph implements Pipeline {
    private String jobName;
    private ExecutionConfig config;
    private Map<Integer, StreamNode> streamNodes;

    private Set<Integer> sources;
    private Set<Integer> sinks;

    //state backend
}

