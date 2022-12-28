package com.kronos.utils;

import com.google.common.annotations.VisibleForTesting;
import com.kronos.jobgraph.logical.LogicalGraph;
import com.kronos.jobgraph.logical.TransformerLogicalNode;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.TPhysicalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jackila
 * @Date: 19:56 2022-12-16
 */
public class GraphUtil {
    public static JoinPhysicalGraph convertToPhysicalGraph(LogicalGraph streamGraph) {
        TransformerLogicalNode root = streamGraph.getRoot();
        TPhysicalNode tPhysicalNode = convertToPhysicalNode(root, null);
        JoinPhysicalGraph graph = new JoinPhysicalGraph(tPhysicalNode, streamGraph.getSinkerId(),
                                                        streamGraph.getMapping());

        return graph;
    }

    @VisibleForTesting
    public static TPhysicalNode convertToPhysicalNode(TransformerLogicalNode node,
                                                      TPhysicalNode root) {
        if (node.getChild() == null || node.getChild().isEmpty()) {
            return new TPhysicalNode(node, root);
        }

        TPhysicalNode proot = new TPhysicalNode(node, root);
        if (root == null) {
            root = proot;
        }
        List<TPhysicalNode> childNodes = new ArrayList<>();
        for (TransformerLogicalNode child : node.getChild()) {
            childNodes.add(convertToPhysicalNode(child, root));
        }

        proot.setNodes(childNodes);
        return proot;
    }
}
