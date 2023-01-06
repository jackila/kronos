package com.kronos.cdc.data.sink.es;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 该数据类型对应object类型，意味着存在多条记录，是一个数组类型 */
@Data
@NoArgsConstructor
public class EsNestTableFieldInfo extends EsBasicFieldInfo {

    /** 内表的映射关系 */
    protected List<EsPrimaryFieldInfo> fieldInfos;

    public Column2FieldInfo toMapping() {
        return new Column2FieldInfo(this.getSourceTable(), this.getFieldInfos());
    }
}
