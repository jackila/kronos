package com.kronos.jobgraph.physic.operator.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.ItemValue;
import com.kronos.cdc.data.RowSourceTip;
import com.kronos.jobgraph.physic.operator.request.DeleteRequest;
import com.kronos.jobgraph.physic.operator.request.DeleteResponse;
import com.kronos.jobgraph.physic.operator.request.InsertRequest;
import com.kronos.jobgraph.physic.operator.request.InsertResponse;
import com.kronos.jobgraph.physic.operator.request.QueryRequest;
import com.kronos.jobgraph.physic.operator.request.QueryResponse;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 线上环境不能使用，只作为集成测试使用 */
@VisibleForTesting
public class MemoryWarehouseManager implements DataWarehouseManager {

    private ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> reservoir;

    public MemoryWarehouseManager() {
        reservoir = new ConcurrentHashMap<>();
    }

    @Override
    public QueryResponse select(QueryRequest request) {
        List<Map<String, FieldItem>> items = getItems(request.getTarget());
        List<ItemValue> data =
                items.stream()
                        .map(
                                item -> {
                                    FieldItem value = item.get(request.getConditionFieldName());
                                    int index = request.getValue().indexOf(value);
                                    if (index >= 0) {
                                        return new ItemValue(
                                                request.getTarget(),
                                                item,
                                                buildSourceTips(request, index));
                                    } else {
                                        return null;
                                    }
                                })
                        .filter(t -> t != null)
                        .collect(Collectors.toList());
        return new QueryResponse(data, request.getTarget());
    }

    private RowSourceTip buildSourceTips(QueryRequest request, int index) {
        RowSourceTip rowSourceTip = null;
        if (request.getSourceTip() != null) {
            rowSourceTip = new RowSourceTip(request.getSourceTip());
            rowSourceTip.setSourceValue(request.getValue().get(index));
        }
        return rowSourceTip;
    }

    @Override
    public InsertResponse insert(InsertRequest request) {
        ObjectPath target = request.getTarget();
        Map<String, FieldItem> value =
                Arrays.stream(request.getValue())
                        .filter(p -> p != null)
                        .map(p -> new FieldItem(target, p.getKey(), p.getValue()))
                        .collect(Collectors.toMap(FieldItem::getFieldName, Function.identity()));
        reservoir.putIfAbsent(target, Lists.newArrayList());
        reservoir.get(target).add(value);
        return new InsertResponse();
    }

    @Override
    public DeleteResponse delete(DeleteRequest request) {
        List<Map<String, FieldItem>> items = getItems(request.getTarget());
        items.stream()
                .filter(
                        item -> {
                            FieldItem value = item.get(request.getConditionFieldName());
                            return request.getValue().equals(value);
                        })
                .forEach(item -> items.remove(item));
        return new DeleteResponse();
    }

    private List<Map<String, FieldItem>> getItems(ObjectPath target) {
        return reservoir.getOrDefault(target, Lists.newArrayList());
    }
}
