package com.kronos.core.delegation;

import com.kronos.core.operations.Operation;

import java.util.List;

/**
 * todo
 * @Author: jackila
 * @Date: 11:44 AM 2022-6-18
 */
public interface Parser {

    List<Operation> parse(String statement);

}
