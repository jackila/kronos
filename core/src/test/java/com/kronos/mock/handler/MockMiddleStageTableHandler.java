package com.kronos.mock.handler;

import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import com.kronos.mock.MockTPhysicalNode;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jackila
 * @Date: 18:03 2022-12-16
 */
public class MockMiddleStageTableHandler extends MockStageTableHandler {

    public MockMiddleStageTableHandler() {

    }

    public MockMiddleStageTableHandler(TPhysicalNode node) {
        super(node);
    }

    @Override
    public AbstractTableTransformerHandler setNode(TPhysicalNode node) {
        this.checkedValues = ((MockTPhysicalNode) node).getCheckMiddleStageFinishedTable();
        return super.setNode(node);
    }

    @Override
    public void onEvent(StreamRecord<List<String>> event,
                        long sequence,
                        boolean endOfBatch) throws Exception {
        if (node.getTarget().getObjectName() != null) {
            event.value().add(node.getTarget().getObjectName());
        }
        Assert.assertTrue(checkedValues.size() + 1 == event.value().size());
        for (String tableName : checkedValues) {
            List<String> value = new ArrayList<>(event.value());

            Assert.assertTrue(
                    String.format("[%s] the table %s not exist in %s", node.getTarget().getObjectName()
                            , tableName, String.join(",", value)), value.contains(tableName));

            long count = value.stream().filter(v -> tableName.equalsIgnoreCase(v)).count();
            Assert.assertEquals("count is " + count, 1l, count);
            if (!tableName.equalsIgnoreCase(node.getTarget().getObjectName())) {
                event.value().remove(tableName);
            }
        }
        Assert.assertEquals(2, event.value().size());
    }
}
