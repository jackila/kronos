package com.kronos.mock;

import com.google.common.collect.Sets;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.HashSet;
import java.util.Set;

/** */
public class MockTPhysicalNode extends TPhysicalNode {

    private Set<String> checkBackStageFinishedTable = Sets.newHashSet();
    private Set<String> checkMiddleStageFinishedTable = Sets.newHashSet();
    private Set<String> checkFrontStageFinishedTable = Sets.newHashSet();

    public MockTPhysicalNode(ObjectPath tableInfo) {
        super(tableInfo);
    }

    @Override
    public TPhysicalNode addChildNode(TPhysicalNode node) {
        super.addChildNode(node);
        ((MockTPhysicalNode) node)
                .addBackStageFinishedTable(target.getObjectName(), checkBackStageFinishedTable);
        this.addFrontStageFinishedTable(
                node.getTarget().getObjectName(),
                ((MockTPhysicalNode) node).getCheckFrontStageFinishedTable());
        return this;
    }

    public MockTPhysicalNode addParentNode(TPhysicalNode node) {
        checkBackStageFinishedTable.add(node.getTarget().getObjectName());
        checkBackStageFinishedTable.addAll(((MockTPhysicalNode) node).checkBackStageFinishedTable);
        checkFrontStageFinishedTable.addAll(checkBackStageFinishedTable);
        return this;
    }

    private void addBackStageFinishedTable(String objectName, Set<String> finishedTable) {
        this.checkBackStageFinishedTable.add(objectName);
        this.checkBackStageFinishedTable.addAll(finishedTable);
    }

    private void addFrontStageFinishedTable(String objectName, Set<String> finishedTable) {
        this.checkFrontStageFinishedTable.add(objectName);
        this.checkFrontStageFinishedTable.addAll(finishedTable);
    }

    public Set<String> getCheckBackStageFinishedTable() {
        return checkBackStageFinishedTable;
    }

    public Set<String> getCheckFrontStageFinishedTable() {
        return checkFrontStageFinishedTable;
    }

    public void addMiddleStageFinishedTable(Set<String> checkBackStageFinishedTable) {
        this.checkMiddleStageFinishedTable = new HashSet<>(checkBackStageFinishedTable);
    }

    public Set<String> getCheckMiddleStageFinishedTable() {
        return checkMiddleStageFinishedTable;
    }
}
