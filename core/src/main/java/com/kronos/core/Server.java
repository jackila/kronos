package com.kronos.core;

import com.kronos.api.connector.source.lib.NumberSequenceSource;
import com.kronos.runtime.jobmaster.JobMaster;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 可以设计成一个分布式任务处理集群，由客户端提交任务，本地执行
 * <p>
 * like canal manager --- canal instance
 *
 * @Author: jackila
 * @Date: 11:02 AM 2022-5-29
 */
public class Server {

    // job master
    public static void main(String[] args) throws Exception {

        // 节点启动

        // 加载配置，build logical gragh

        NumberSequenceSource source = new NumberSequenceSource(0, 100);
        // 返回一个StreamGraph
        //job master
        //获取coorinator

        JobMaster master = new JobMaster(source);
        master.onStart();
        List<Future> workStatus = null;//master.deploy(streamGraph);

        // where all task finished
        for (Future status : workStatus) {
            status.get();
        }
        System.exit(0);

        // deploy task，just multi thread
        // 构造disruptor ring buffer

        //构造producer

        //启动线程
    }
}
