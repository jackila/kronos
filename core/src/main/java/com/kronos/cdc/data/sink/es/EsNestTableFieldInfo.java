package com.kronos.cdc.data.sink.es;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 该数据类型对应object类型，意味着存在多条记录，是一个数组类型
 *
 * @Author: jackila
 * @Date: 15:32 2020-10-10
 */
@Data
@NoArgsConstructor
public class EsNestTableFieldInfo extends EsBasicFieldInfo {

    /**
     * 内表的映射关系
     */
    protected List<EsPrimaryFieldInfo> fieldInfos;
}
