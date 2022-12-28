package com.kronos.cdc.data.source;

import lombok.Getter;

import java.util.Map;

/**
 * @Author: jackila
 * @Date: 19:53 2022/12/17
 */
@Getter
public class SourceOffset {
    private static final String FILE_LABEL = "file";
    private static final String POS_LABEL = "pos";

    public SourceOffset(Map source) {
        this.file = String.valueOf(source.get(FILE_LABEL));
        this.pos = Long.valueOf(String.valueOf(source.get(POS_LABEL)));
    }

    private String file;
    private Long pos;
}
