package com.kronos.mock;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import java.util.List;

/** */
public class MockEmptyHandler extends AbstractTableTransformerHandler<StreamRecord<List<String>>> {

    @Override
    public void catchEventChange(StreamRecord<List<String>> event) {
        // do nothing
    }

    @Override
    public boolean doHandler(StreamRecord<List<String>> event) {
        return true;
    }
}
