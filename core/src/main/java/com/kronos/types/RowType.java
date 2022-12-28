package com.kronos.types;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 20:14 2022-12-11
 */
public class RowType {
    private String name;
    private String typeName;

    public RowType(String name,
                   String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getFieldCount() {
        return 1;
    }

    public List<String> getFieldNames() {
        return Lists.newArrayList(name);
    }
}
