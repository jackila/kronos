package com.kronos.cdc.data.sink.es;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 所谓的【来源】是指该数据通过什么表中哪个字段的值获取到的
 *
 * @Author: jackila
 * @Date: 16:18 2020-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceInfo implements Serializable {

    /**
     * 数据来源表
     */
    private ObjectPath sourceTable;

    /**
     * 来源字段
     */
    private String sourceColumn;

    /**
     * 来源字段对应的值
     */
    private Object sourceValue;


}
