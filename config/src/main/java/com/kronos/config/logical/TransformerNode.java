package com.kronos.config.logical;

import com.kronos.config.table.ObjectPath;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 11:06 AM 2022-6-22
 */
public class TransformerNode extends AbstractTableNode {
    private List<TransformerNode> child;

    public TransformerNode(ObjectPath target) {
        super(target);
    }
}
