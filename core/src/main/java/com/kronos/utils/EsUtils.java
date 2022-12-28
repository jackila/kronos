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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kronos.cdc.data.FieldItem;
import com.kronos.cdc.data.sink.es.Column2FieldInfo;
import com.kronos.cdc.data.sink.es.EsConfigKeys;
import com.kronos.cdc.data.sink.es.EsNestTableFieldInfo;
import com.kronos.cdc.data.sink.es.EsPrimaryFieldInfo;
import com.kronos.cdc.data.sink.es.SourceInfo;
import com.kronos.jobgraph.table.ObjectPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Utilities for ElasticSearch
 * <p>
 * Company: www.dtstack.com
 *
 * @author huyifan.zju@163.com
 */
public class EsUtils {

    public static Logger logger = LoggerFactory.getLogger(EsUtils.class);

    //递归算法
    public static Map buildTrunkJsonMap(ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> container,
                                        Column2FieldInfo mappingInfo) {

        List<Map> retJsonMaps = buildNestedJsonMap(container, mappingInfo.getDefaultTable(), mappingInfo.getMapping()
                , null, null, null

        );
        if (!retJsonMaps.isEmpty()) {
            return retJsonMaps.get(0);
        }
        return Maps.newHashMap();
    }

    /**
     * 先构建主表的数据、并作为递归的一个缓解
     *
     * @param container
     * @param table
     * @param fieldInfos
     * @param masterTable
     * @param masterInnerVal
     * @return
     */
    private static List<Map> buildNestedJsonMap(ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> container,
                                                ObjectPath table,
                                                List<EsPrimaryFieldInfo> fieldInfos,
                                                ObjectPath masterTable,
                                                Map masterInnerVal,
                                                String columnExpansion) {

        List<Map> ret = Lists.newArrayList();
        List<Map<String, FieldItem>> records = container.getOrDefault(table, Lists.newArrayList());
        if (!records.isEmpty()) {
            for (Map value : records) {
                Map innerVal = buildInnerJson(value, fieldInfos, container, table);
                if (StringUtils.isNotBlank(columnExpansion)) {
                    fillExpansionField(innerVal, value, columnExpansion);
                }
                ret.add(innerVal);
            }
        }
        return ret;
    }

    /**
     * 判断是否已经存在，如果存在则跳过
     *
     * @param innerVal
     * @param tableValue
     * @param columnExpansion
     */
    private static void fillExpansionField(Map innerVal,
                                           Map tableValue,
                                           String columnExpansion) {

        if (StringUtils.isNotBlank(columnExpansion)) {
            String[] split = columnExpansion.split(":");
            String c_key = split[0];
            String c_value = split[1];
            if (tableValue.containsKey(c_key) && tableValue.containsKey(c_value)) {
                String key = String.valueOf(tableValue.get(c_key));
                String value = String.valueOf(tableValue.get(c_value));
                if (!innerVal.containsKey(key)) {
                    innerVal.put(key, value);
                }
            }
        }

    }

    public static SourceInfo mapToObject(Map<String, Object> map,
                                         Class<SourceInfo> beanClass) throws Exception {
        if (map == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        SourceInfo sourceInfo = objectMapper.convertValue(map, beanClass);
        return sourceInfo;
    }

    /**
     * 验证这条数据的来源是否是该masterTable
     * 如果sourceInfos没有，直接返回true
     * <p>
     * <p>
     * sourceInfos的数据结构 com.jkys.xdts.flink.transform.com.jkys.xdts.flink.ots.bean.TableValSourceInfo
     *
     * @param sourceInfos
     * @param masterTable
     * @param masterInnerVal
     * @param container
     * @return
     */
    private static boolean checkRightSource(List<SourceInfo> sourceInfos,
                                            ObjectPath masterTable,
                                            Map masterInnerVal,
                                            ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> container) {

        if (sourceInfos.isEmpty()) {
            return false;
        }
        for (SourceInfo sourceInfo : sourceInfos) {

            ObjectPath table = sourceInfo.getSourceTable();
            String sourceColumn = sourceInfo.getSourceColumn();
            Object sourceValue = sourceInfo.getSourceValue();
            if (masterTable.equals(table)) {

                Object val = masterInnerVal.get(sourceColumn);
                return String.valueOf(val).equals(String.valueOf(sourceValue));
            } else {

                List<Map> data = Lists.newArrayList();
                List<SourceInfo> infos = Lists.newArrayList();
                List<Map<String, FieldItem>> records = container.getOrDefault(table, Lists.newArrayList());
                for (int i = 0; i < records.size(); i++) {
                    Map cr = records.get(i);
                    Map value = (Map) cr.getOrDefault("value", Maps.newHashMap());
                    if (String.valueOf(sourceValue).equals(String.valueOf(value.get(sourceColumn)))) {
                        data.addAll((List) cr.getOrDefault("sourceInfo", Lists.newArrayList()));
                    }
                }

                if (data.isEmpty()) {
                    return false;
                }

                for (Map infoMap : data) {
                    try {
                        infos.add(mapToObject(infoMap, SourceInfo.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return checkRightSource(infos, masterTable, masterInnerVal, container);
            }

        }
        return false;
    }

    private static Object fetchDirectValue(Map<String, FieldItem> value,
                                           String type,
                                           String sourceField,
                                           String format,
                                           Integer precision,
                                           String toFormat) {
        FieldItem col = null;
        if (value.containsKey(sourceField)) {
            col = value.get(sourceField);
        }
        return col.getValue();
    }

    private static Map buildInnerJson(Map val,
                                      List<EsPrimaryFieldInfo> columnInfos,
                                      ConcurrentHashMap<ObjectPath, List<Map<String, FieldItem>>> container,
                                      ObjectPath defaultTable) {
        Map innerVal = Maps.newHashMap();
        Set<String> arrayLable = Sets.newHashSet();
        for (EsPrimaryFieldInfo columnInfo : columnInfos) {
            String type = columnInfo.getType();
            String format = columnInfo.getFormat();
            String fieldName = columnInfo.getName();
            ObjectPath table = columnInfo.getSourceTable();
            String field = columnInfo.getSourceField();
            Integer precision = columnInfo.getPrecision();

            String toFormat = columnInfo.getToFormat();

            if (StringUtils.isNotBlank(format)) {
                format = format.replace(",", " ");
            }

            String association = columnInfo.getAssociation();

            if (columnInfo.isArray()) {
                if (!defaultTable.equals(table)) {
                    List<Map<String, FieldItem>> records = container.getOrDefault(table, Lists.newArrayList());
                    if (!records.isEmpty()) {
                        for (Map<String, FieldItem> record : records) {
                            Object valObj = fetchDirectValue(record, type, field, format, precision, toFormat);
                            if (valObj != null) {
                                List arrayData = (List) innerVal.getOrDefault(fieldName, Lists.newArrayList());
                                arrayLable.add(fieldName);
                                arrayData.add(valObj);
                                innerVal.put(fieldName, arrayData);
                            }
                        }

                    }
                }
            } else if ("object".equals(type)) {

                EsNestTableFieldInfo nestTable = columnInfo.getNestTable();
                List<Map> innerContainer = buildNestedJsonMap(container, nestTable.getSourceTable(),
                                                              nestTable.getFieldInfos(), defaultTable, val,
                                                              columnInfo.getColumnExpansion());

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
                 * 那么courseName是取第一个、还是取数组、还是用特殊字符结合起来
                 * 当前默认取第一个
                 */
                Map tempVal = Maps.newHashMap();
                if (!defaultTable.equals(table)) {
                    List<Map<String, FieldItem>> records = container.getOrDefault(table, Lists.newArrayList());

                    if (!records.isEmpty()) {
                        tempVal = records.get(0);
                    }
                }

                Object valObj = fetchDirectValue(!defaultTable.equals(table) ? tempVal : val, type, field, format,
                                                 precision, toFormat);

                if (valObj != null) {
                    if (columnInfo.isArray()) {
                        List arrayData = (List) innerVal.getOrDefault(fieldName, Lists.newArrayList());
                        arrayLable.add(fieldName);
                        arrayData.add(valObj);
                        innerVal.put(fieldName, arrayData);
                    } else {
                        innerVal.put(fieldName, valObj);
                    }
                }
            }
        }

        innerVal = listToArray(innerVal, arrayLable);

        return innerVal;
    }

    /**
     * 输出到es中的应该是数组
     *
     * @param innerVal
     * @param arrayLable
     * @return
     */
    private static Map listToArray(Map innerVal,
                                   Set<String> arrayLable) {
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

    /**
     * 特殊处理地理位置
     *
     * @param innerVal
     * @param fieldName
     * @param valObj
     * @return
     */
    private static Map setGeoPointValue(Map innerVal,
                                        String fieldName,
                                        Object valObj) {
        String[] columns = fieldName.split("\\.");
        if (columns.length != 2) {
            String errorMess = String.format("geo_point的格式必须是location.lat 或者是 location.lon,字段%s不符合", fieldName);
            logger.error(errorMess);
            throw new RuntimeException(errorMess);
        }
        String colunm = columns[0];
        String properties = columns[1];
        Map location = (Map) innerVal.getOrDefault(colunm, Maps.newHashMap());

        //Mercator 投影精确度
        if ("lat".equals(properties) && ((double) valObj > 90 || (double) valObj < -90)) {
            logger.error("维度{} 超出范围，sink库中该值会被默认设置为0：  data={}", valObj, innerVal);
            valObj = 0.0d;
        }

        if ("lon".equals(properties) && ((double) valObj > 180 || (double) valObj < -180)) {
            logger.error("经度{} 超出范围，sink库中该值会被默认设置为0： data={}", valObj, innerVal);
            valObj = 0.0d;
        }

        location.put(properties, valObj);
        innerVal.put(colunm, location);
        return innerVal;
    }
}
