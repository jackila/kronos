package com.kronos.cdc.data;

import static com.kronos.jobgraph.physic.operator.handler.sink.ESSinkFunctionHandler.LOCAL_REQUEST_DATA;

import com.google.common.collect.Lists;
import com.kronos.jobgraph.table.ObjectPath;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** */
@Data
public class ItemValue {
    public ItemValue(
            ObjectPath target, Map<String, FieldItem> columnValues, RowSourceTip sourceTip) {
        this.target = target;
        this.columnValues = columnValues;
        this.sourceTip = sourceTip;
    }

    public ItemValue(ObjectPath target, Map<String, FieldItem> columnValues) {
        this.target = target;
        this.columnValues = columnValues;
    }

    private ObjectPath target;
    private Map<String, FieldItem> columnValues;
    private RowSourceTip sourceTip;

    public boolean isFrom(ItemValue sourceItem) {
        if (sourceItem == null) {
            return true;
        }
        return checkSourceItem(Lists.newArrayList(sourceTip), sourceItem);
    }

    /**
     * 验证这条数据的来源是否是该masterTable 如果sourceInfos没有，直接返回true.
     *
     * <p>sourceInfos的数据结构
     * com.jkys.xdts.flink.transform.com.jkys.xdts.flink.ots.bean.TableValSourceInfo
     *
     * @param sourceInfos
     * @return
     */
    private static boolean checkSourceItem(List<RowSourceTip> sourceInfos, ItemValue sourceItem) {

        if (sourceInfos.isEmpty()) {
            return false;
        }
        for (RowSourceTip sourceInfo : sourceInfos) {

            ObjectPath table = sourceInfo.getSourceTable();
            String sourceColumn = sourceInfo.getSourceColumn();
            Object sourceValue = sourceInfo.getSourceValue();
            if (sourceItem.getTarget().equals(table)) {
                Object val = sourceItem.getColumnValues().get(sourceColumn);
                return String.valueOf(val).equals(String.valueOf(sourceValue));
            } else {

                List<RowSourceTip> infos = Lists.newArrayList();
                List<ItemValue> records =
                        LOCAL_REQUEST_DATA.get().getOrDefault(table, Lists.newArrayList());
                for (int i = 0; i < records.size(); i++) {
                    ItemValue cr = records.get(i);
                    Map value = cr.getColumnValues();
                    if (String.valueOf(sourceValue).equals(String.valueOf(value.get(sourceColumn)))
                            && cr.getSourceTip() != null) {
                        infos.addAll(Lists.newArrayList(cr.getSourceTip()));
                    }
                }
                return checkSourceItem(infos, sourceItem);
            }
        }
        return false;
    }
}
