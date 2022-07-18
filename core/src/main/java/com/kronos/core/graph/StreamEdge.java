package com.kronos.core.graph;

/**
 * @Author: jackila
 * @Date: 3:13 PM 2022-6-18
 */
public class StreamEdge {
    private final String edgeId;

    private final int sourceId;
    private final int targetId;

    /**
     * The name of the operator in the source vertex.
     */
    private final String sourceOperatorName;

    /**
     * The name of the operator in the target vertex.
     */
    private final String targetOperatorName;

    public StreamEdge(
            StreamNode sourceVertex,
            StreamNode targetVertex
    ) {

        this.sourceId = sourceVertex.getId();
        this.targetId = targetVertex.getId();
        this.sourceOperatorName = sourceVertex.getOperatorName();
        this.targetOperatorName = targetVertex.getOperatorName();
        this.edgeId =
                sourceVertex + "_" + targetVertex;
    }
}
