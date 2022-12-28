package com.kronos.jobgraph.physic.operator;

/**
 * @Author: jackila
 * @Date: 10:20 PM 2022-7-25
 */
public interface StreamOperator {
    void open() throws Exception;
    void close() throws Exception;
}
