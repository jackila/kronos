package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.table.ObjectPath;
import lombok.Getter;
import lombok.Setter;

/*
 *
 *                                    x              x
 *                                    x              x
 * ┌───────┐      ┌────────────────┐  x              x  ┌───────────────┐         ┌────────┐
 * │teacher├──────► optional course│  x              x  │optional course├─────────►teacher │
 * └───────┘      └────────┬───────┘  x              x  └──────▲────────┘         └────────┘
 *                         │          x              x         │
 *                         │          x   ┌───────┐  x         │
 *                         ├──────────────►student├────────────┤
 *                         │          x   └───────┘  x         │
 *                    ┌────┴───┐      x              x    ┌────▼────┐
 *                    │  grade │      x              x    │  grade  │
 *                    └────────┘      x              x    └─────────┘
 *                                    x              x
 *  ──────────────────────────────────────────────────────────────────────────────────────────
 *                   front stage      x middle stage x     back stage
 *                                    x              x
 *                                    x              x
 *
 *  我们易上面的例子，解释findTarget\findField field的关系
 *
 *  关系： student.grade_id = grade.id
 *
 *  假设现在grade数据已知，求student的数据记录
 *  则当前operator的node为student
 *  此时需要在该operator中的List<QueryCondition>中找到QueryConditio={findTarget=grade,findField=id, field=grade_id}
 *  并通过findTarget(grade)\findField(id) 获取值 x
 *  构造查询语句： select * from student where grade_id = find(grade,id)
 *
 *  上述为front阶段，各节点的核心算法
 *
 *  对于处于back阶段，算法如下
 *  假设已知student的数据，求grade的记录
 *  此时operator中唯一字段 QueryCondition parentCondition; 为确定的 {findTarget=student,findField=grade_id, field=id}
 *  通过findTarget(student)\findField(grade_id) 获取值 x
 *  构造查询语句： select * from grade where id = find(student,grade_id)
 *
 */

/** */
@Getter
@Setter
public class QueryCondition {
    private ObjectPath findTarget;
    private String findField;
    private String field;

    public QueryCondition(ObjectPath findTarget, String findField, String field) {
        this.findTarget = findTarget;
        this.findField = findField;
        this.field = field;
    }
}
