package com.kronos.api.connector.source;

/**
 * @Author: jackila
 * @Date: 11:46 2022-10-15
 */
public class ReaderInfo {
    private final int subtaskId;

    public ReaderInfo(int subtaskId) {
        this.subtaskId = subtaskId;
    }

    public int subtaskId() {
        return subtaskId;
    }
}
