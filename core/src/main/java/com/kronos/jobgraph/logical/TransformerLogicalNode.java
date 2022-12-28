package com.kronos.jobgraph.logical;

import com.google.common.collect.Lists;
import com.kronos.api.tuple.Tuple2;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 11:06 AM 2022-6-22
 */
@Getter
public class TransformerLogicalNode extends AbstractTableNode {
    private List<TransformerLogicalNode> child;

    public TransformerLogicalNode(ObjectPath target) {
        super(target);
    }

    /**
     * when stage is in front, we search data by this relation
     * student.grade_id = grade.id
     * 如果当前节点是student（graph 是student ---> grade）
     * tuple2.f0 : {grade, id}
     * tuple2.f1: grade_id
     * <p>
     * so the search query will be
     * select * from student where grade_id = find(grade,id)
     */
    List<QueryCondition> childRelevanceUsedInFrontStage;
    /**
     * current node is grade
     * then
     * tuple2.f0 : id
     * tuple2.f1 : {student,grade_id}
     * <p>
     * so the search query will be
     * select * from grade where id = find(student,grade_id)
     */
    QueryCondition parentRelevanceUsedInBackStage;

    public TransformerLogicalNode(Tuple2<RelevanceInfo, RelevanceInfo> child,
                                  ObjectPath parentTarget) {
        RelevanceInfo current = child.f0;
        RelevanceInfo parent = child.f1;

        if (child.f0.getTarget().equals(parentTarget)) {
            current = child.f1;
            parent = child.f0;
        }

        parentRelevanceUsedInBackStage = new QueryCondition(parent.getTarget(), parent.getColumnName(),
                                                            current.getColumnName());
        this.target = current.getTarget();
    }

    public void initChildRelevanceUsedInFrontStage(List<Tuple2<RelevanceInfo, RelevanceInfo>> children) {
        childRelevanceUsedInFrontStage = Lists.newArrayList();
        for (Tuple2<RelevanceInfo, RelevanceInfo> child : children) {
            RelevanceInfo current = child.f1;
            RelevanceInfo next = child.f0;

            if (child.f0.getTarget().equals(target)) {
                current = child.f0;
                next = child.f1;
            }
            childRelevanceUsedInFrontStage.add(new QueryCondition(next.getTarget(), next.getColumnName(),
                                                                  current.getColumnName()));
        }
    }

    public TransformerLogicalNode addChild(TransformerLogicalNode target) {
        if (child == null) {
            child = Lists.newArrayList();
        }
        child.add(target);
        return this;
    }

    public List<TransformerLogicalNode> getChild() {
        return child;
    }

}
