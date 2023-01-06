package com.kronos.mock.handler;

import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import com.kronos.mock.MockTPhysicalNode;

/** */
public class MockFrontStageTableHandler extends MockStageTableHandler {

    public MockFrontStageTableHandler() {
        super();
    }

    public MockFrontStageTableHandler(TPhysicalNode node) {
        super(node);
    }

    @Override
    public AbstractTableTransformerHandler setNode(TPhysicalNode node) {
        this.checkedValues = ((MockTPhysicalNode) node).getCheckFrontStageFinishedTable();
        return super.setNode(node);
    }
}
