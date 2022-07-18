package com.kronos.core;

import com.kronos.core.executor.StreamExecutor;
import com.kronos.core.operations.Operation;

import java.util.List;

/**
 * @Author: jackila
 * @Date: 11:02 AM 2022-5-29
 */
public class Server {

    public static void main(String[] args) {

        // 节点启动

        // 加载配置，返回logical gragh
        // logical gragh 转换为 physical node

        String config = "";
        //启动 source node
        StreamExecutor executor = new StreamExecutor();
        List<Operation> operations = executor.getParser().parse(config);

        // getStreamGraph()
        //executor.executeQuery((QueryOperation) operation);

    }
}
