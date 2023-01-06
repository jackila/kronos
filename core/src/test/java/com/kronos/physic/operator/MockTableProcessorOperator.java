package com.kronos.physic.operator;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.TableProcessOperator;

/** */
public class MockTableProcessorOperator extends TableProcessOperator {
    @Override
    public void onEvent(StreamRecord event, long sequence, boolean endOfBatch) throws Exception {
        super.onEvent(event, sequence, endOfBatch);
    }
}
