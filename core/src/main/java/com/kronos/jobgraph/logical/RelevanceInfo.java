package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @Author: jackila
 * @Date: 17:43 2022/12/18
 */
@Getter
@Setter
public class RelevanceInfo {
    private ObjectPath target;
    private String columnName;

    public RelevanceInfo(ObjectPath target,
                         String columnName) {
        this.target = target;
        this.columnName = columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof RelevanceInfo)) return false;

        RelevanceInfo that = (RelevanceInfo) o;

        return new EqualsBuilder().append(target, that.target).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(target).toHashCode();
    }
}
