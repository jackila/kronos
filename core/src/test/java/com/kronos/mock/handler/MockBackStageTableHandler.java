package com.kronos.mock.handler;

import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import com.kronos.mock.MockTPhysicalNode;

/**
 * @Author: jackila
 * @Date: 18:04 2022-12-16
 */
public class MockBackStageTableHandler extends MockStageTableHandler{

    public MockBackStageTableHandler() {
        super();
    }

    public MockBackStageTableHandler(TPhysicalNode node) {
        super(node);
    }

    @Override
    public AbstractTableTransformerHandler setNode(TPhysicalNode node) {
        this.checkedValues = ((MockTPhysicalNode)node).getCheckBackStageFinishedTable();
        return super.setNode(node);
    }
}
