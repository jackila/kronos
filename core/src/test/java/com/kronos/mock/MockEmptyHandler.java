package com.kronos.mock;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 15:08 2022-12-16
 */
public class MockEmptyHandler extends AbstractTableTransformerHandler<StreamRecord<List<String>>> {

    @Override
    public void catchEventChange(StreamRecord<List<String>> event) {
       //do nothing
    }

    @Override
    public boolean doHandler(StreamRecord<List<String>> event) {
        return true;
    }
}
