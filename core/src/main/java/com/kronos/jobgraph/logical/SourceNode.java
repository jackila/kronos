package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;

/** 支持多种模式，如全量+增量 */
public class SourceNode extends AbstractTableNode {
    public SourceNode(ObjectPath target) {
        super(target);
    }
}
