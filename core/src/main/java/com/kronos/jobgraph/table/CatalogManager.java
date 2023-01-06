package com.kronos.jobgraph.table;

import com.kronos.jobgraph.JobConfiguration;
import com.kronos.jobgraph.raw.Sinker;
import com.kronos.jobgraph.table.database.CatalogDatabase;
import com.kronos.jobgraph.table.database.ElasticsearchCatalogDatabase;
import com.kronos.jobgraph.table.database.JDBCCatalogDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/** 元数据 catalog 管理多个catalog database catalog database 管理多个 catalog table 2022-6-22 */
@Data
public class CatalogManager {
    private static CatalogManager catalogManager;
    private List<CatalogDatabase> databases;

    public CatalogManager() {
        databases = new ArrayList<>();
    }

    public void register(JobConfiguration config) {

        this.register(
                config.getDataSources(),
                (datasource) -> databases.add(JDBCCatalogDatabase.build(datasource)));
        this.register(config.getSinker());
        this.register(
                config.getTableInfos(),
                (tableInfo -> {
                    CatalogDatabase catalogDatabase = fetchDatabase(tableInfo.getDatabase());
                    catalogDatabase.registerTable(
                            new ObjectPath(tableInfo.getDatabase(), tableInfo.getTableName()));
                }));
    }

    private <T> void register(List<T> values, Consumer<T> handler) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (T value : values) {
            handler.accept(value);
        }
    }

    private void register(Sinker sinker) {
        if (sinker == null) {
            return;
        }
        CatalogDatabase build = ElasticsearchCatalogDatabase.build(sinker);
        build.setSinker(true);
        databases.add(build);
    }

    private CatalogDatabase fetchDatabase(String name) {
        return databases.stream()
                .filter(p -> name.equalsIgnoreCase(p.specificName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("not found " + name));
    }

    public static CatalogManager getInstance() {
        if (catalogManager == null) {
            catalogManager = new CatalogManager();
        }
        return catalogManager;
    }

    public CatalogDatabase findSinkerCatalog() {
        return databases.stream()
                .filter(p -> p.isSinker())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("not found sinker "));
    }

    public ObjectPath findObjectPathByTable(String table) {

        if (StringUtils.isEmpty(table)) {
            throw new RuntimeException("table can not be empty");
        }

        if (table.contains("\\.")) {
            String[] info = table.split("\\.");
            return new ObjectPath(info[0], info[1]);
        }

        String database =
                databases.stream()
                        .filter(
                                t -> {
                                    return t.checkTable(table)
                                            && t.getDatabaseType() != DatabaseType.ES;
                                })
                        .map(p -> p.specificName())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("not fount table " + table));
        return new ObjectPath(database, table);
    }
}
