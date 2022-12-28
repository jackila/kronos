package com.kronos.jobgraph.physic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kronos.cdc.data.sink.es.Column2FieldInfo;
import com.kronos.cdc.data.sink.es.EsNestTableFieldInfo;
import com.kronos.cdc.data.sink.es.EsPrimaryFieldInfo;
import com.kronos.jobgraph.raw.Mapper;
import com.kronos.jobgraph.table.CatalogManager;
import com.kronos.jobgraph.table.ObjectPath;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * @Author: jackila
 * @Date: 18:43 2022-12-14
 */
public class JoinPhysicalGraph {

    private TPhysicalNode root;
    private String primaryKeyFields;
    private Column2FieldInfo mapping;

    public JoinPhysicalGraph(TPhysicalNode root,
                             String sinkerId,
                             List<Mapper> mappers) {
        this.root = root;
        if (StringUtils.isNotBlank(sinkerId)) {
            String[] split = sinkerId.split("\\.");
            this.primaryKeyFields = split[split.length - 1];
        } else {
            this.primaryKeyFields = "id";
        }

        this.mapping = new Column2FieldInfo(root.getTarget(), convertFromMapper(mappers));
    }

    private List<EsPrimaryFieldInfo> convertFromMapper(List<Mapper> mappers) {

        if (mappers == null || mappers.isEmpty()) {
            return null;
        }
        List<EsPrimaryFieldInfo> mappingResult = Lists.newArrayList();
        for (Mapper mapper : mappers) {
            List<Mapper> mappingFields = mapper.getMapping();
            if (mappingFields == null || mappingFields.isEmpty()) {
                EsPrimaryFieldInfo field = new EsPrimaryFieldInfo(mapper);
                mappingResult.add(field);
            } else {
                ObjectPath target = CatalogManager.getInstance().findObjectPathByTable(mapper.getSource());

                EsPrimaryFieldInfo field = new EsPrimaryFieldInfo();
                field.setName(mapper.getField());
                field.setSourceTable(target);
                field.setType("object");
                List<EsPrimaryFieldInfo> fieldInfos = convertFromMapper(mappingFields);

                EsNestTableFieldInfo nestTableFieldInfo = new EsNestTableFieldInfo();
                nestTableFieldInfo.setSourceTable(target);
                nestTableFieldInfo.setFieldInfos(fieldInfos);
                field.setNestTable(nestTableFieldInfo);

                mappingResult.add(field);
            }
        }
        return mappingResult;
    }

    public Set<ObjectPath> involvedTarget() {
        Set<ObjectPath> ret = Sets.newHashSet();
        Deque<TPhysicalNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TPhysicalNode head = stack.poll();
            ret.add(head.getTarget());
            if (head.getNodes() == null || head.getNodes().isEmpty()) {
                continue;
            }
            for (TPhysicalNode node : head.getNodes()) {
                stack.push(node);
            }
        }
        return ret;
    }

    public JoinPhysicalGraph(TPhysicalNode root) {
        this.root = root;
    }

    public TPhysicalNode getRoot() {
        return root;
    }

    public void setRoot(TPhysicalNode root) {
        this.root = root;
    }

    public String getPrimaryKeyFields() {
        return primaryKeyFields;
    }

    public Column2FieldInfo getMapping() {
        return mapping;
    }
}
