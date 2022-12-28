package com.kronos.physic.operator;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.operator.TableProcessOperator;

/**
 * @Author: jackila
 * @Date: 2:56 PM 2022-7-24
 */
public class MockTableProcessorOperator extends TableProcessOperator {
    @Override
    public void onEvent(StreamRecord event, long sequence, boolean endOfBatch) throws Exception {
        super.onEvent(event, sequence, endOfBatch);

    }
}
