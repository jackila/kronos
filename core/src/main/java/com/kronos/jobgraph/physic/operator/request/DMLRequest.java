package com.kronos.jobgraph.physic.operator.request;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Data;

/**
 * @Author: jackila
 * @Date: 19:50 2022/12/19
 */
@Data
public class DMLRequest {
    protected ObjectPath target;
}
