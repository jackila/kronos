package com.kronos.jobgraph.common;

/**
 * @Author: jackila
 * @Date: 2:19 PM 2022-6-18
 */
public class ExecutionConfig {

    private int parallelism = 1;

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }
}
