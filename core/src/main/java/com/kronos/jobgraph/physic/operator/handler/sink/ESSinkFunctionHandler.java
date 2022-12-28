package com.kronos.jobgraph.physic.operator.handler.sink;

import com.kronos.cdc.data.ControllerRecord;
import com.kronos.cdc.data.sink.RecordSet;
import com.kronos.cdc.data.sink.es.Column2FieldInfo;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.table.database.ElasticsearchCatalogDatabase;
import com.kronos.utils.EsUtils;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.client.Requests;
import org.kronos.base.ElasticsearchSinkFunction;
import org.kronos.base.RequestIndexer;

import java.util.Date;
import java.util.Map;

/**
 * @Author: jackila
 * @Date: 22:01 2022/12/26
 */
public class ESSinkFunctionHandler implements ElasticsearchSinkFunction<RecordSet> {
    private ElasticsearchCatalogDatabase configureInfo;
    private JoinPhysicalGraph graph;

    private Column2FieldInfo esFieldInfo;

    private String esPrimaryKey;

    public ESSinkFunctionHandler(ElasticsearchCatalogDatabase configureInfo,
                                 JoinPhysicalGraph graph) {

        this.configureInfo = configureInfo;
        this.graph = graph;
        this.esPrimaryKey = graph.getPrimaryKeyFields();
        this.esFieldInfo = graph.getMapping();
    }

    @Override
    public void process(RecordSet element,
                        RequestIndexer indexer) {

        ActionRequest actionRequest = createIndexRequest(element);

        if (actionRequest != null) {
            indexer.add(actionRequest);
        }
    }

    private ActionRequest createIndexRequest(RecordSet element) {
        boolean deleted = isMasterDeleted(element.getController(), graph.getRoot());
        return deleted ? createDeleteRequest(element) : createUpsertRequest(element);
    }

    private ActionRequest createUpsertRequest(RecordSet element) {
        Map sinkVal = EsUtils.buildTrunkJsonMap(element.getItems(), esFieldInfo);

        if (!sinkVal.isEmpty()) {
            sinkVal.put("upsertTime", new Date());
            String id = String.valueOf(sinkVal.get(esPrimaryKey));
            return Requests.indexRequest().index(configureInfo.getIndex()).type("_doc").id(id).source(sinkVal);
        }
        return null;
    }

    private ActionRequest createDeleteRequest(RecordSet element) {
        return null;
    }

    private boolean isMasterDeleted(ControllerRecord controller,
                                    TPhysicalNode root) {
        return controller.isMasterTableDeleteOperation(root);
    }
}
