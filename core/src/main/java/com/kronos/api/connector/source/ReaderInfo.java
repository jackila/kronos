package com.kronos.api.connector.source;

/** */
public class ReaderInfo {
    private final int subtaskId;

    public ReaderInfo(int subtaskId) {
        this.subtaskId = subtaskId;
    }

    public int subtaskId() {
        return subtaskId;
    }
}
