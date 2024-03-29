package com.kronos.jobgraph.raw;

import java.util.List;

/** */
public class Mapper {
    private String field;
    private String source;
    private List<Mapper> mapping;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Mapper> getMapping() {
        return mapping;
    }

    public void setMapping(List<Mapper> mapping) {
        this.mapping = mapping;
    }
}
