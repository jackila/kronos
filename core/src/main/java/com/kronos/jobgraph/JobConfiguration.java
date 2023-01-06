package com.kronos.jobgraph;

import com.google.common.collect.Lists;
import com.kronos.api.tuple.Tuple2;
import com.kronos.jobgraph.logical.RelevanceInfo;
import com.kronos.jobgraph.raw.DataSource;
import com.kronos.jobgraph.raw.Mapper;
import com.kronos.jobgraph.raw.Sinker;
import com.kronos.jobgraph.raw.TableInfo;
import com.kronos.jobgraph.table.ObjectPath;
import io.debezium.annotation.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/** */
public class JobConfiguration {
    private List<DataSource> dataSources;
    private List<TableInfo> tableInfos;
    private List<String> relations;
    private Sinker sinker;

    public ObjectPath getMainTable() {
        return tableInfos.stream()
                .filter(t -> t.isMainTable())
                .map(t -> new ObjectPath(t.getDatabase(), t.getTableName()))
                .findAny()
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "not config main table,the config need mainTable"));
    }

    public List<Tuple2<RelevanceInfo, RelevanceInfo>> getConnectRelation() {
        List<Tuple2<RelevanceInfo, RelevanceInfo>> ret = Lists.newArrayList();
        for (String relation : relations) {
            String[] relationColumns = relation.split("=");
            if (relationColumns.length != 2) {
                throw new RuntimeException("relation need like X.x = Y.y");
            }
            RelevanceInfo leftConnection = findRelationTable(relationColumns[0]);
            RelevanceInfo rightConnection = findRelationTable(relationColumns[1]);
            ret.add(Tuple2.of(leftConnection, rightConnection));
        }
        return ret;
    }

    @VisibleForTesting
    public RelevanceInfo findRelationTable(String table) {
        String[] info = table.split("\\.");
        String tableName = "";
        String column = "";
        if (info.length == 2) {
            tableName = info[0].trim();
            column = info[1].trim();
        } else {
            throw new RuntimeException("relation config should be set as: x.x = y.y");
        }
        for (TableInfo tableInfo : tableInfos) {
            if (tableName.equalsIgnoreCase(tableInfo.getTableName())) {
                return new RelevanceInfo(
                        new ObjectPath(tableInfo.getDatabase(), tableName), column);
            }
        }
        throw new RuntimeException(
                "the table " + table + " from relation can not find in tableInfo");
    }

    @SneakyThrows
    public static JobConfiguration load(String name) {
        Yaml yaml = new Yaml(new Constructor(JobConfiguration.class));
        InputStream resourceAsStream = new FileInputStream(new File(name));
        return yaml.load(resourceAsStream);
    }

    public List<DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public List<TableInfo> getTableInfos() {
        return tableInfos;
    }

    public void setTableInfos(List<TableInfo> tableInfos) {
        this.tableInfos = tableInfos;
    }

    public List<String> getRelations() {
        return relations;
    }

    public void setRelations(List<String> relations) {
        this.relations = relations;
    }

    public Sinker getSinker() {
        return sinker;
    }

    public void setSinker(Sinker sinker) {
        this.sinker = sinker;
    }

    public String sinkerPrimaryKey() {
        return this.sinker.getId();
    }

    public List<Mapper> sinkerMapper() {
        return this.sinker.getMapping();
    }
}
