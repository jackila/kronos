package com.kronos.cdc.data;

import com.google.common.collect.Lists;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.data.source.SourceOffset;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.physic.operator.handler.StageType;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 当前暂时不处理 外键变更的场景
 *
 * @Author: jackila
 * @Date: 12:13 2022/12/18
 */
@Data
public class ControllerRecord implements Record {

    //event table
    private ObjectPath target;
    // type 类型
    private RowKind type;
    // need handler table
    private List<ObjectPath> tablePathInFrontStage = Lists.newArrayList();
    private int cursor = -1;
    private SourceOffset sourcePosition;

    private FieldItem primaryKey;

    public ControllerRecord(ControllerRecord source) {
        this.target = source.getTarget();
        this.type = source.getType();
        this.sourcePosition = source.getSourcePosition();
        this.primaryKey = source.getPrimaryKey();
    }

    public ControllerRecord(DtsRecord source) {
        this.type = source.getType();
        this.target = source.getTarget();
        this.sourcePosition = source.getSourcePosition();
        this.primaryKey = source.getPrimaryKey();
    }
    // 是否执行该算子的handler 逻辑： delete、front、end

    public ObjectPath getPreTarget() {

        if (cursor - 1 < 0) {
            return target;
        }
        return tablePathInFrontStage.get(cursor - 1);
    }

    /**
     * @param node
     * @param stageType
     * @return
     */
    public boolean doHandle(TPhysicalNode node,
                            StageType stageType) {
        // not save operator
        if (isMasterTableDeleteOperation(node)) {
            return false;
        }

        switch (stageType) {
            case FRONT:
            case MIDDLE:
                boolean pathNode = tablePathInFrontStage.contains(node.getTarget());
                if (pathNode) cursor++;
                return pathNode;
            case BACK:
            default:
                return true;
        }
    }

    public boolean isMasterTableDeleteOperation(TPhysicalNode node) {
        return type == RowKind.DELETE && node.getRoot().getTarget().equals(target);
    }

    public void setHandlerTable(Map<ObjectPath, List<ObjectPath>> targetPath) {
        this.tablePathInFrontStage = targetPath.get(target);
    }
}
