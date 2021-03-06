package com.kronos.config.raw;

/**
 * 数据库连接信息
 * @Author: jackila
 * @Date: 10:59 AM 2022-5-29
 */
public class DataSource {
    private  String schema;
    private String url;
    private String user;
    private String password;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
