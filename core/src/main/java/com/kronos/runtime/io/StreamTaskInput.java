package com.kronos.runtime.io;

/** */
public interface StreamTaskInput extends PushingAsyncDataInput {

    // in flink this task can do snapshot and get input index

    /** Returns the input index of this input. */
    int getInputIndex();
}
