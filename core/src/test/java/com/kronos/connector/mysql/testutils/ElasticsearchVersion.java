package com.kronos.connector.mysql.testutils;

/**
 * @Author: jackila
 * @Date: 22:05 2022/12/27
 */
public enum ElasticsearchVersion {
    V7_17("7.17.8"),
    V7_9_2("7.9.2");
    private String version;

    ElasticsearchVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ElasticsearchVersion{" + "version='" + version + '\'' + '}';
    }
}
