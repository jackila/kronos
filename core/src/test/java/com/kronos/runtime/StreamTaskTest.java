package com.kronos.runtime;


import com.kronos.runtime.taskmanager.RuntimeEnvironment;
import com.kronos.runtime.tasks.SourceOperatorStreamTask;
import com.kronos.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @Author: jackila
 * @Date: 2:03 PM 2022-7-24
 */
class StreamTaskTest {

    StreamTask streamTask;
    @BeforeEach
    public void setup(){
        streamTask = new SourceOperatorStreamTask(0, new RuntimeEnvironment(null,null));
        TestUtils.createMockSource(streamTask);
    }

    /**
     * 收到一个有序的事件：1、2、3、4、5
     * 插入一个表X的event handler后，返回的记录需要保证如下结果
     * 1. 返回的记录顺序不会发生变化，依然是(1、2、3、4、5)
     * 2. 每条记录中都已经被表X的event handler处理，check一下mock后的record即可
     */
    @Test
    public void checkInsertSingleEventHandler(){

        //streamTask.insertSingleEventHandler();
    }

    /**
     * 一个拓扑chain
     */
    @Test
    public void testApplyLinkedChaining(){

    }

    @Test
    public void testApplyParallelChaing(){

    }

    @Test
    public void testApplyLinkedWithParallelChaing(){

    }

    @Test
    public void testTaskRun(){

    }
}