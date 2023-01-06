/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.utils;

import static com.kronos.jobgraph.physic.operator.handler.sink.ESSinkFunctionHandler.LOCAL_REQUEST_DATA;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.ItemValue;
import com.kronos.cdc.data.sink.es.Column2FieldInfo;
import com.kronos.cdc.data.sink.es.EsConfigKeys;
import com.kronos.cdc.data.sink.es.EsNestTableFieldInfo;
import com.kronos.cdc.data.sink.es.EsPrimaryFieldInfo;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for ElasticSearch */
public class EsUtils {

    public static Logger logger = LoggerFactory.getLogger(EsUtils.class);

    // 递归算法
    public static Map buildTrunkJsonMap(Column2FieldInfo mappingInfo) {

        List<Map> retJsonMaps = buildNestedJsonMap(mappingInfo, null);

        if (!retJsonMaps.isEmpty()) {
            return retJsonMaps.get(0);
        }
        return Maps.newHashMap();
    }

    /**
     * 先构建主表的数据、并作为递归的一个缓解
     *
     * @return
     */
    private static List<Map> buildNestedJsonMap(
            Column2FieldInfo mappingInfo, ItemValue parentItem) {
        List<ItemValue> records =
                LOCAL_REQUEST_DATA
                        .get()
                        .getOrDefault(mappingInfo.getDefaultTable(), Lists.newArrayList());
        List<Map> ret =
                records.stream()
                        .filter(item -> item.getSourceTip() == null || item.isFrom(parentItem))
                        .map(itemValue -> buildInnerJson(itemValue, mappingInfo))
                        .collect(Collectors.toList());
        return ret;
    }

    private static Object fetchDirectValue(Map<String, FieldItem> value, String sourceField) {
        FieldItem col = null;
        if (value.containsKey(sourceField)) {
            col = value.get(sourceField);
        } else {
            return null;
        }
        return col.getValue();
    }

    private static Map buildInnerJson(ItemValue item, Column2FieldInfo mappingInfo) {
        Map innerVal = Maps.newHashMap();
        Set<String> arrayLable = Sets.newHashSet();
        for (EsPrimaryFieldInfo columnInfo : mappingInfo.getMapping()) {
            String fieldName = columnInfo.getName();
            ObjectPath table = columnInfo.getSourceTable();
            String field = columnInfo.getSourceField();
            String association = columnInfo.getAssociation();

            if ("object".equals(columnInfo.getType())) {
                EsNestTableFieldInfo nestTable = columnInfo.getNestTable();
                List<Map> innerContainer = buildNestedJsonMap(nestTable.toMapping(), item);
                if (!innerContainer.isEmpty()) {
                    if (EsConfigKeys.ONE_TO_ONE.equalsIgnoreCase(association)) {
                        innerVal.put(fieldName, innerContainer.get(0));
                        if (innerContainer.size() > 1) {
                            logger.warn("{} 当前模式为OneToOne,但是发现存在多条记录", fieldName);
                        }
                    } else {
                        innerVal.put(fieldName, innerContainer);
                    }
                }
            } else {
                /**
                 * 如果表非默认表，那么就面对取其他表的数据，此时需要用户指定如何组装记录，比如student对应多个course
                 * 那么courseName是取第一个、还是取数组、还是用特殊字符结合起来 当前默认取第一个
                 */
                Map targetTableValue = item.getColumnValues();
                if (!mappingInfo.getDefaultTable().equals(table)) {
                    List<ItemValue> records =
                            LOCAL_REQUEST_DATA.get().getOrDefault(table, Lists.newArrayList());
                    List<ItemValue> targetItems = fetchSpecificItemBySourceTips(records, item);
                    if (!targetItems.isEmpty()) {
                        targetTableValue = targetItems.get(0).getColumnValues();
                    }
                }

                Object valObj = fetchDirectValue(targetTableValue, field);
                if (valObj != null) {
                    innerVal.put(fieldName, valObj);
                }
            }
        }

        innerVal = listToArray(innerVal, arrayLable);

        return innerVal;
    }

    private static List<ItemValue> fetchSpecificItemBySourceTips(
            List<ItemValue> records, ItemValue item) {
        return records.stream()
                .filter(checkedItem -> checkedItem.isFrom(item) || item.isFrom(checkedItem))
                .collect(Collectors.toList());
    }

    /**
     * 输出到es中的应该是数组
     *
     * @param innerVal
     * @param arrayLable
     * @return
     */
    private static Map listToArray(Map innerVal, Set<String> arrayLable) {
        if (arrayLable.isEmpty()) {
            return innerVal;
        }
        for (String fieldName : arrayLable) {
            Object[] dynType = new Object[0];
            if (innerVal.containsKey(fieldName)) {
                List values = (List) innerVal.getOrDefault(fieldName, Lists.newArrayList());
                innerVal.put(fieldName, values.toArray(dynType));
            }
        }
        return innerVal;
    }
}
