package com.kronos.config.logical;

import com.kronos.config.table.ObjectPath;

/**
 * @Author: jackila
 * @Date: 7:26 PM 2022-6-23
 */
public abstract class AbstractTableNode implements Node {

    ObjectPath target;

    public AbstractTableNode(ObjectPath target) {
        this.target = target;
    }

    public ObjectPath getTarget() {
        return target;
    }
}
