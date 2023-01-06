package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;

/** */
public abstract class AbstractTableNode implements Node {

    ObjectPath target;

    public AbstractTableNode() {}

    public AbstractTableNode(ObjectPath target) {
        this.target = target;
    }

    public ObjectPath getTarget() {
        return target;
    }
}
