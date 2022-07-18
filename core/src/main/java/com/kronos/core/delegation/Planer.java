package com.kronos.core.delegation;

import com.kronos.core.dag.Transformation;

import java.util.List;

/**
 *  该接口有两个作用
 *  1> sql parse via getParse(), translate a sql string into tree of operator
 *  2> relational planer 将某个json配置转换为可运行的一个任务
 * @Author: jackila
 * @Date: 11:37 AM 2022-6-18
 */
public interface Planer {
    /**
     * Retrieves a {@link Parser} that provides methods for parsing a SQL string.
     *
     * @return initialized {@link Parser}
     */
    Parser getParser();

    /**
     * Converts a json plan into a set of runnable {@link Transformation}s.
     *
     * <p>The json plan is the string json representation of an optimized ExecNode plan for the
     * given statement. An ExecNode plan can be serialized to json plan, and a json plan can be
     * deserialized to an ExecNode plan.
     *
     * <p>NOTES: Only the Blink planner supports this method.
     *
     * <p><b>NOTES:</b>: This is an experimental feature now.
     *
     * @param yamlPlan The yaml plan to be translated.
     * @return list of corresponding {@link Transformation}s.
     */
    List<Transformation<?>> translateJsonPlan(String yamlPlan);
}
