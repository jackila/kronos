package com.kronos.runtime.io;

/**
 * @Author: jackila
 * @Date: 6:18 PM 2022-8-02
 */
public interface StreamTaskInput extends PushingAsyncDataInput{

    // in flink this task can do snapshot and get input index

    /** Returns the input index of this input. */
    int getInputIndex();

}
