package com.kronos.jobgraph.table.database;

import com.kronos.jobgraph.raw.Sinker;
import com.kronos.jobgraph.table.DatabaseType;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Builder;
import lombok.Data;
import org.apache.http.HttpHost;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jackila
 * @Date: 15:24 2022/12/27
 */
@Data
@Builder
public class ElasticsearchCatalogDatabase extends CatalogDatabase {
    private String index;
    private String type;
    private int port;
    private String id;

    private String host;

    private int bulkAction = 100;
    private int bulkSizeMb = 0;
    private int bulkIntervalMs = 200;
    private boolean backoffEnable = true;
    private String backoffType = "CONSTANT";
    private int backoffDelay = 200;
    private int backoffRetries = 3;

    public static CatalogDatabase build(Sinker sinker) {
        ElasticsearchCatalogDatabase build = new ElasticsearchCatalogDatabaseBuilder()
                .index(sinker.getIndex())
                .id(sinker.getId())
                .bulkAction(100)
                .bulkSizeMb(0)
                .bulkIntervalMs(200)
                .backoffEnable(true)
                .backoffType("CONSTANT")
                .backoffDelay(200)
                .backoffRetries(3)
                .build();
        build.setDatabaseType(DatabaseType.ES);
        build.setUsername(sinker.getUsername());
        build.setPassword(sinker.getPassword());

        build.setHost(sinker.getHost());
        return build;
    }

    @Override
    public String specificName() {
        return index;
    }

    @Override
    public void registerTable(ObjectPath objectPath) {
        throw new RuntimeException("un support register table");
    }

    @Override
    public boolean checkTable(String table) {
        return table.equalsIgnoreCase(index);
    }

    public List<HttpHost> httpHosts() {
        List<HttpHost> ret = new ArrayList<>();
        String[] ht = host.split(",");
        for (String addr : ht) {
            String[] hostInfo = addr.split(":");
            ret.add(new HttpHost(hostInfo[0], Integer.valueOf(hostInfo[1])));
        }
        return ret;
    }
}
