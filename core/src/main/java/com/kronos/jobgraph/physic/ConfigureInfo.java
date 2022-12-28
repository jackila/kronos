package com.kronos.jobgraph.physic;

import lombok.Builder;
import lombok.Data;
import org.apache.http.HttpHost;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 19:36 2020-10-20
 */
@Data
@Builder
public class ConfigureInfo {
    private List<HttpHost> httpHosts;
    private String address;
    private String username;
    private String password;
    private String index;
    private String type;

    private int bulkAction;
    private int bulkSizeMb;
    private int bulkIntervalMs;
    private boolean backoffEnable;
    private String backoffType;
    private int backoffDelay;
    private int backoffRetries;
}
