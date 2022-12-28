package com.kronos.jobgraph.physic;

import com.google.common.collect.Lists;
import com.kronos.jobgraph.logical.QueryCondition;
import com.kronos.jobgraph.logical.TransformerLogicalNode;
import com.kronos.jobgraph.physic.disruptor.ProcessorInput;
import com.kronos.jobgraph.physic.disruptor.ProcessorOutput;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * transformer physical node
 *
 * @Author: jackila
 * @Date: 18:47 2022-12-14
 */
@Getter
@Setter
public class TPhysicalNode extends StreamNode {
    private TPhysicalNode root;
    private List<TPhysicalNode> nodes;
    private int parallel = 1;
    private ProcessorInput input;
    private ProcessorOutput output;
    protected ObjectPath target;
    protected Map<ObjectPath, QueryCondition> childRelevanceInFrontStage;
    private QueryCondition parentRelevanceUsedInBackStage;

    public TPhysicalNode(ObjectPath tableInfo) {
        this.target = tableInfo;
    }

    public TPhysicalNode(TransformerLogicalNode node,TPhysicalNode root) {
        input = new ProcessorInput();
        output = new ProcessorOutput();
        this.root = root;
        this.target = node.getTarget();
        this.parentRelevanceUsedInBackStage = node.getParentRelevanceUsedInBackStage();
        List<QueryCondition> childRelevances = node.getChildRelevanceUsedInFrontStage();
        if (childRelevances != null) {
            this.childRelevanceInFrontStage =
                    childRelevances.stream().collect(Collectors.toMap(QueryCondition::getFindTarget,
                                                                      Function.identity()));
        }
    }

    public QueryCondition findQuerConditionInPrepare(ObjectPath target) {
        return childRelevanceInFrontStage.get(target);
    }
    public List<TPhysicalNode> getNodes() {
        return nodes;
    }

    public TPhysicalNode addChildNode(TPhysicalNode node) {
        if (nodes == null) {
            nodes = Lists.newArrayList();
        }
        nodes.add(node);
        return this;
    }

    public boolean isRoot(){
        return root == this;
    }

    public void setNodes(List<TPhysicalNode> nodes) {
        this.nodes = nodes;
    }

    public ProcessorInput getInput() {
        return input;
    }

    public void setInput(ProcessorInput input) {
        this.input = input;
    }

    public ProcessorOutput getOutput() {
        return output;
    }

    public void setOutput(ProcessorOutput output) {
        this.output = output;
    }

    public void setInput(ProcessorOutput output) {
        this.input = new ProcessorInput(output.output());
    }

    public int getParallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    public ObjectPath getTarget() {
        return target;
    }

    public void setTarget(ObjectPath target) {
        this.target = target;
    }
}
