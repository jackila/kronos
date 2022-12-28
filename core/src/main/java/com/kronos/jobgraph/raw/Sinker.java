package com.kronos.jobgraph.raw;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 12:21 PM 2022-5-29
 */
public class Sinker {
    private String type;
    private String host;
    private String username;
    private String password;
    private String index;
    private String id;
    private List<Mapper> mapping;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Mapper> getMapping() {
        return mapping;
    }

    public void setMapping(List<Mapper> mapping) {
        this.mapping = mapping;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
