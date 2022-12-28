package com.kronos.mock.handler;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.handler.AbstractTableTransformerHandler;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: jackila
 * @Date: 17:49 2022-12-15
 */
public class MockStageTableHandler extends AbstractTableTransformerHandler<StreamRecord<List<String>>> {

    protected Set<String> checkedValues = new HashSet<>();
    public MockStageTableHandler() {
    }

    public MockStageTableHandler(TPhysicalNode node) {
        setNode(node);
    }

    @Override
    public void catchEventChange(StreamRecord<List<String>> event) {
        Assert.assertTrue(checkedValues.size() + 1 <= event.value().size());
        for (String tableName : checkedValues) {
            List<String> value = new ArrayList<>(event.value());

            Assert.assertTrue(
                    String.format("[%s] the table %s not exist in %s", node.getTarget().getObjectName()
                            , tableName, String.join(",", value)), value.contains(tableName));

            long count = value.stream().filter(v -> tableName.equalsIgnoreCase(v)).count();
            Assert.assertEquals("count is " + count, 1l, count);
        }

        if (node.getTarget().getObjectName() != null) {
            event.value().add(node.getTarget().getObjectName());
        }
    }

    @Override
    public QueryCondition findQueryCondition(DiffStageRecords eventVal) {
        return null;
    }

    @Override
    public boolean doHandler(StreamRecord<List<String>> event) {
        return true;
    }
}
