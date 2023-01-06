package com.kronos.cdc.data.sink.es;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/**
 * 一般情况下是一对一的配置.
 *
 * <p>name: sourceField: sourceTable: type:
 *
 * <p>todo 如果取另一种表的某个字段，存在多个值的情况下如何处理
 *
 * <p>如果type是object，意味着将另一个表的数据加入该主表中 关键信息：字段名，表名，该表的字段映射关系 2020-10-10
 */
@Data
public class EsBasicFieldInfo {

    /** es 索引中字段名. */
    private String name;

    /** 该字段数据的来源：表 */
    private ObjectPath sourceTable;
    /** es中对应字段的类型 */
    private String type;

    /** 针对object类型，存在OneToOne、OneToMany类型 */
    private String association;

    /** 针对时间的格式化处理 */
    private String format;

    /** 针对浮点数精度的设置，保留小数位 */
    private Integer precision;

    /** 在es中存储的格式，是否为array类型 */
    private boolean array;

    /** 格式化输出到es中的数据类型 */
    private String toFormat;

    /** 是否为行转列字段，主要用于动态增加这种类型 */
    private String columnExpansion;
}
