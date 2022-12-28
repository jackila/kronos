package com.kronos.jobgraph.physic.operator.handler;

import com.google.common.collect.Maps;
import com.kronos.cdc.data.ControllerRecord;
import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.StreamRecord;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.table.ObjectPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: jackila
 * @Date: 19:17 2022-12-16
 */
public class ControllerHandler extends AbstractTableTransformerHandler<StreamRecord<DiffStageRecords>> {

    private final Map<ObjectPath, List<ObjectPath>> targetPath = Maps.newHashMap();

    public ControllerHandler(JoinPhysicalGraph graph) {
        travel(graph.getRoot(), new ArrayList<>());
    }

    @Override
    public void catchEventChange(StreamRecord<DiffStageRecords> event) {

        ControllerRecord controller = new ControllerRecord(event.value().getSource());
        controller.setHandlerTable(targetPath);

        event.value().setController(controller);
    }

    @Override
    public boolean doHandler(StreamRecord<DiffStageRecords> event) {
        return true;
    }

    private boolean travel(TPhysicalNode head,
                           List<ObjectPath> path) {
        if (head == null) {
            return false;
        }

        targetPath.put(head.getTarget(), new ArrayList<>(path));
        path.add(head.getTarget());
        if (head.getNodes() == null || head.getNodes().isEmpty()) {
            return true;
        }
        for (TPhysicalNode node : head.getNodes()) {
            boolean add = travel(node, path);
            if (add) {
                path.remove(path.size() - 1);
            }
        }
        return true;
    }
}
