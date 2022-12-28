package com.kronos.jobgraph.logical;

import com.kronos.api.tuple.Tuple2;
import com.kronos.jobgraph.JobConfiguration;
import com.kronos.jobgraph.raw.Mapper;
import com.kronos.jobgraph.table.CatalogManager;
import com.kronos.jobgraph.table.ObjectPath;
import io.debezium.annotation.VisibleForTesting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jackila
 * @Date: 11:10 AM 2022-6-22
 */
public class LogicalGraph {
    private List<SinkNode> sinkNodes;
    private List<SourceNode> sourceNodes;
    private TransformerLogicalNode root;
    private String sinkerId;
    private List<Mapper> mapping;

    public LogicalGraph build(JobConfiguration config) {
        //register catalog
        CatalogManager.getInstance().register(config);
        root = convertToHandlerTree(config);
        this.sinkerId = config.sinkerPrimaryKey();
        this.mapping = config.sinkerMapper();
        return this;
    }

    @VisibleForTesting
    public TransformerLogicalNode convertToHandlerTree(JobConfiguration config) {

        ObjectPath mainTable = config.getMainTable();
        List<Tuple2<RelevanceInfo, RelevanceInfo>> connectRelation = config.getConnectRelation();

        TransformerLogicalNode root = new TransformerLogicalNode(mainTable);

        Deque<TransformerLogicalNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!connectRelation.isEmpty()) {
            TransformerLogicalNode traverRoot = stack.poll();
            ObjectPath parentTarget = traverRoot.getTarget();
            List<Tuple2<RelevanceInfo, RelevanceInfo>> children = findRelations(traverRoot, connectRelation);
            if (children.isEmpty()) {
                continue;
            }

            traverRoot.initChildRelevanceUsedInFrontStage(children);
            for (Tuple2<RelevanceInfo, RelevanceInfo> child : children) {
                TransformerLogicalNode node = new TransformerLogicalNode(child, parentTarget);
                traverRoot.addChild(node);
                stack.push(node);
            }
            connectRelation.removeAll(children);
        }
        return root;
    }

    private List<Tuple2<RelevanceInfo, RelevanceInfo>> findRelations(TransformerLogicalNode traverRoot,
                                                                     List<Tuple2<RelevanceInfo, RelevanceInfo>> connectRelation) {
        return connectRelation.stream().filter(r -> r.f0.getTarget().equals(traverRoot.target) || r.f1.getTarget().equals(traverRoot.target))
                .collect(Collectors.toList());
    }

    public List<SinkNode> getSinkNodes() {
        return sinkNodes;
    }

    public List<SourceNode> getSourceNodes() {
        return sourceNodes;
    }

    public TransformerLogicalNode getRoot() {
        return root;
    }

    public List<Mapper> getMapping() {
        return mapping;
    }

    public String getSinkerId() {
        return sinkerId;
    }
}
