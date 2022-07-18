package com.kronos.core.graph;

import com.kronos.core.common.io.InputFormat;
import com.kronos.core.common.io.OutputFormat;
import com.kronos.core.operators.StreamOperatorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jackila
 * @Date: 2:25 PM 2022-6-18
 */
public class StreamNode {

    private final int id;
    private final String operatorName;
    private StreamOperatorFactory<?> operatorFactory;
    // 入边集合
    private List<StreamEdge> inEdges = new ArrayList<StreamEdge>();
    // 出边集合
    private List<StreamEdge> outEdges = new ArrayList<StreamEdge>();

    //private final Class<? extends AbstractInvokable> jobVertexClass;

    private InputFormat<?, ?> inputFormat;
    private OutputFormat<?> outputFormat;

    public StreamNode(
            Integer id,
            StreamOperatorFactory<?> operatorFactory,
            String operatorName
            ) {
        this.id = id;
        this.operatorName = operatorName;
        this.operatorFactory = operatorFactory;
    }

    public int getId() {
        return id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public StreamOperatorFactory<?> getOperatorFactory() {
        return operatorFactory;
    }

    public List<StreamEdge> getInEdges() {
        return inEdges;
    }

    public List<StreamEdge> getOutEdges() {
        return outEdges;
    }

    public InputFormat<?, ?> getInputFormat() {
        return inputFormat;
    }

    public OutputFormat<?> getOutputFormat() {
        return outputFormat;
    }
}
