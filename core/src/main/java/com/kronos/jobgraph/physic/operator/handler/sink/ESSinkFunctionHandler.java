package com.kronos.jobgraph.physic.operator.handler.sink;

import com.kronos.cdc.data.ControllerRecord;
import com.kronos.cdc.data.ItemValue;
import com.kronos.cdc.data.sink.RecordSet;
import com.kronos.cdc.data.sink.es.Column2FieldInfo;
import com.kronos.jobgraph.physic.JoinPhysicalGraph;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.table.ObjectPath;
import com.kronos.jobgraph.table.database.ElasticsearchCatalogDatabase;
import com.kronos.utils.EsUtils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.client.Requests;
import org.kronos.base.ElasticsearchSinkFunction;
import org.kronos.base.RequestIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class ESSinkFunctionHandler implements ElasticsearchSinkFunction<RecordSet> {
    private ElasticsearchCatalogDatabase configureInfo;
    private JoinPhysicalGraph graph;
    private Column2FieldInfo esFieldInfo;
    private String esPrimaryKey;
    private static Logger logger = LoggerFactory.getLogger(ESSinkFunctionHandler.class);

    public static final ThreadLocal<ConcurrentHashMap<ObjectPath, List<ItemValue>>>
            LOCAL_REQUEST_DATA;

    static {
        LOCAL_REQUEST_DATA = new ThreadLocal();
    }

    public ESSinkFunctionHandler(
            ElasticsearchCatalogDatabase configureInfo, JoinPhysicalGraph graph) {

        this.configureInfo = configureInfo;
        this.graph = graph;
        this.esPrimaryKey = graph.getPrimaryKeyFields();
        this.esFieldInfo = graph.getMapping();
    }

    @Override
    public void process(RecordSet element, RequestIndexer indexer) {

        try {
            LOCAL_REQUEST_DATA.set(element.getItems());
            ActionRequest actionRequest = createIndexRequest(element);
            if (actionRequest != null) {
                indexer.add(actionRequest);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            LOCAL_REQUEST_DATA.remove();
        }
    }

    private ActionRequest createIndexRequest(RecordSet element) {
        boolean deleted = isMasterDeleted(element.getController(), graph.getRoot());
        return deleted ? createDeleteRequest(element) : createUpsertRequest(element);
    }

    private ActionRequest createUpsertRequest(RecordSet element) {
        Object id = getRequestId(element);
        Map sinkVal = EsUtils.buildTrunkJsonMap(esFieldInfo);

        if (!sinkVal.isEmpty() && id != null) {
            sinkVal.put("upsertTime", new Date());
            return Requests.indexRequest()
                    .index(configureInfo.getIndex())
                    .type("_doc")
                    .id(String.valueOf(id))
                    .source(sinkVal);
        }
        return null;
    }

    private Object getRequestId(RecordSet element) {
        Object id = element.getPrimaryKey().getValue();
        if (id == null) {
            logger.error("the element can not find primary key ,and skip it {}", element);
        }
        return id;
    }

    private ActionRequest createDeleteRequest(RecordSet element) {

        Object requestId = getRequestId(element);
        if (requestId != null) {
            return Requests.deleteRequest(configureInfo.getIndex()).id(String.valueOf(requestId));
        }
        return null;
    }

    private boolean isMasterDeleted(ControllerRecord controller, TPhysicalNode root) {
        return controller.isMasterTableDeleteOperation(root);
    }
}
