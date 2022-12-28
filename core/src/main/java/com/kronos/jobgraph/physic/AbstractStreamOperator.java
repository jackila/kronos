package com.kronos.jobgraph.physic;

import com.kronos.jobgraph.physic.operator.StreamOperator;
import com.lmax.disruptor.EventHandler;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * just a interface but do nothing
 * @Author: jackila
 * @Date: 10:00 AM 2022-7-23
 */
public abstract class AbstractStreamOperator implements EventHandler<StreamRecord>, StreamOperator {
    /** The logger used by the operator class and its subclasses. */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractStreamOperator.class);


    private String operatorName;

    private int operatorId;

    @Override
    public void onEvent(StreamRecord event, long sequence, boolean endOfBatch) throws Exception {
        // default is do nothing
    }

    public String operatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public int operatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }
}
