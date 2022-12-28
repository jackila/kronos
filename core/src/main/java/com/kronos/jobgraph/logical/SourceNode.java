package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;

/**
 * 支持多种模式，如全量+增量
 * @Author: jackila
 * @Date: 11:04 AM 2022-6-22
 */
public class SourceNode extends AbstractTableNode{
    public SourceNode(ObjectPath target) {
        super(target);
    }
}
