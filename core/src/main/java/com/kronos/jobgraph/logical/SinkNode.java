package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;

/**
 * @Author: jackila
 * @Date: 11:04 AM 2022-6-22
 */
public class SinkNode extends AbstractTableNode{
    public SinkNode(ObjectPath target) {
        super(target);
    }
}
