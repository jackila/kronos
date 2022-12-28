package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;

/**
 * @Author: jackila
 * @Date: 7:26 PM 2022-6-23
 */
public abstract class AbstractTableNode implements Node {

    ObjectPath target;

    public AbstractTableNode() {
    }

    public AbstractTableNode(ObjectPath target) {
        this.target = target;
    }

    public ObjectPath getTarget() {
        return target;
    }
}
