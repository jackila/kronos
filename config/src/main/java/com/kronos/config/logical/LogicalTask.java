package com.kronos.config.logical;

import com.kronos.config.Config;
import com.kronos.config.table.Catalog;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 11:10 AM 2022-6-22
 */
public class LogicalTask {
    private List<SinkNode> sinkNodes;
    private List<SourceNode> sourceNodes;
    private TransformerNode root;
    private Catalog catalog;

    public LogicalTask build(Config config){

        //register catalog
        catalog.register(config);
        convertToLogiscalNode(config);
        return this;
    }

    private void convertToLogiscalNode(Config config) {
        // source nodes
        //sink nodes
        //root
    }

    public List<SinkNode> getSinkNodes() {
        return sinkNodes;
    }

    public List<SourceNode> getSourceNodes() {
        return sourceNodes;
    }

    public TransformerNode getRoot() {
        return root;
    }

}
