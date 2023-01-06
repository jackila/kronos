package com.kronos.cdc.data.sink.es;

import com.kronos.jobgraph.raw.Mapper;
import com.kronos.jobgraph.table.CatalogManager;
import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;
import lombok.NoArgsConstructor;

/** */
@Data
@NoArgsConstructor
public class EsPrimaryFieldInfo extends EsBasicFieldInfo {

    /** 该字段数据的来源：字段 */
    private String sourceField;

    /** 如果type为object，那么该值存在数据 */
    private EsNestTableFieldInfo nestTable;

    public EsPrimaryFieldInfo(Mapper mapper) {
        String source = mapper.getSource();
        String[] components = source.split("\\.");
        if (components.length == 3) {
            this.setSourceTable(new ObjectPath(components[0], components[1]));
            this.setSourceField(components[2]);
        } else if (components.length == 2) {
            String table = components[0];
            this.setSourceTable(CatalogManager.getInstance().findObjectPathByTable(table));
            this.setSourceField(components[1]);
        } else {
            throw new RuntimeException(
                    "the mapping field is illegal: "
                            + source
                            + " and it should contain table and "
                            + "field like table.field");
        }

        this.setName(mapper.getField());
    }
}
