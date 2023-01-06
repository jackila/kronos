package com.kronos.jobgraph.logical;

import com.google.common.collect.Lists;
import com.kronos.api.tuple.Tuple2;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.debezium.RowDebeziumDeserializationSchema;
import com.kronos.cdc.source.mysql.source.MySqlSource;
import com.kronos.jobgraph.JobConfiguration;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.raw.Mapper;
import com.kronos.jobgraph.table.CatalogManager;
import com.kronos.jobgraph.table.ObjectPath;
import com.kronos.jobgraph.table.database.CatalogDatabase;
import com.kronos.jobgraph.table.database.JDBCCatalogDatabase;
import io.debezium.annotation.VisibleForTesting;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** */
public class LogicalGraph {
    private TransformerLogicalNode root;
    private String sinkerId;
    private List<Mapper> mapping;

    private Source[] sources;

    public LogicalGraph() {}

    public LogicalGraph(
            TransformerLogicalNode root, String sinkerId, List<Mapper> mapping, Source[] sources) {
        this.root = root;
        this.sinkerId = sinkerId;
        this.mapping = mapping;
        this.sources = sources;
    }

    public static LogicalGraph instance(JobConfiguration config) {
        // register catalog
        CatalogManager.getInstance().register(config);
        List<MySqlSource> sources = Lists.newArrayList();
        Map<String, List<CatalogDatabase>> databaseInfos =
                CatalogManager.getInstance().getDatabases().stream()
                        .filter(p -> !p.isSinker())
                        .collect(Collectors.groupingBy(CatalogDatabase::getAddress));
        AtomicInteger serverId = new AtomicInteger(1400);
        databaseInfos.forEach(
                (address, databases) -> {
                    String[] addressInfo = address.split(":");
                    List<String> databaseNames =
                            databases.stream()
                                    .map(d -> d.specificName())
                                    .collect(Collectors.toList());
                    List<String> tables =
                            databases.stream()
                                    .flatMap(p -> ((JDBCCatalogDatabase) p).getTables().stream())
                                    .map(t -> t.getTarget().toString())
                                    .collect(Collectors.toList());
                    MySqlSource source =
                            MySqlSource.<DtsRecord>builder()
                                    .hostname(addressInfo[0])
                                    .port(Integer.valueOf(addressInfo[1]))
                                    .databaseList(databaseNames.toArray(new String[0]))
                                    .tableList(tables.toArray(new String[0]))
                                    .username(databases.get(0).getUsername())
                                    .password(databases.get(0).getPassword())
                                    .serverId(
                                            String.format(
                                                    "%d-%d", serverId.get(), serverId.get() + 3))
                                    .deserializer(new RowDebeziumDeserializationSchema())
                                    .includeSchemaChanges(true) // output the schema changes as well
                                    .build();
                    serverId.addAndGet(1000);
                    sources.add(source);
                });

        return new LogicalGraph(
                convertToHandlerTree(config),
                config.sinkerPrimaryKey(),
                config.sinkerMapper(),
                sources.toArray(new MySqlSource[0]));
    }

    @VisibleForTesting
    public static TransformerLogicalNode convertToHandlerTree(JobConfiguration config) {

        ObjectPath mainTable = config.getMainTable();
        List<Tuple2<RelevanceInfo, RelevanceInfo>> connectRelation = config.getConnectRelation();

        TransformerLogicalNode root = new TransformerLogicalNode(mainTable);

        Deque<TransformerLogicalNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!connectRelation.isEmpty()) {
            TransformerLogicalNode traverRoot = stack.poll();
            ObjectPath parentTarget = traverRoot.getTarget();
            List<Tuple2<RelevanceInfo, RelevanceInfo>> children =
                    findRelations(traverRoot, connectRelation);
            if (children.isEmpty()) {
                continue;
            }

            traverRoot.initChildRelevanceUsedInFrontStage(children);
            for (Tuple2<RelevanceInfo, RelevanceInfo> child : children) {
                TransformerLogicalNode node = new TransformerLogicalNode(child, parentTarget);
                traverRoot.addChild(node);
                stack.push(node);
            }
            connectRelation.removeAll(children);
        }
        return root;
    }

    private static List<Tuple2<RelevanceInfo, RelevanceInfo>> findRelations(
            TransformerLogicalNode traverRoot,
            List<Tuple2<RelevanceInfo, RelevanceInfo>> connectRelation) {
        return connectRelation.stream()
                .filter(
                        r ->
                                r.f0.getTarget().equals(traverRoot.target)
                                        || r.f1.getTarget().equals(traverRoot.target))
                .collect(Collectors.toList());
    }

    public TransformerLogicalNode getRoot() {
        return root;
    }

    public List<Mapper> getMapping() {
        return mapping;
    }

    public String getSinkerId() {
        return sinkerId;
    }

    public Source[] getSources() {
        return sources;
    }
}
