package com.kronos.runtime.tasks;

import com.kronos.jobgraph.physic.operator.source.SourceOperator;
import com.kronos.runtime.StreamTask;
import com.kronos.runtime.execution.Environment;
import com.kronos.runtime.io.StreamInputProcessor;
import com.kronos.runtime.io.StreamTaskInput;
import com.kronos.runtime.io.StreamTaskSourceInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: jackila
 * @Date: 5:02 PM 2022-8-02
 */
public class SourceOperatorStreamTask extends StreamTask<SourceOperator> {
    private int index;
    private static Logger logger = LoggerFactory.getLogger(StreamTask.class);
    public SourceOperatorStreamTask(int index,
                                    Environment env) {
        super(env);
        this.index = index;
    }
    public void init() throws Exception {

        SourceOperator sourceOperator = this.mainOperator;
        sourceOperator.initReader();

        final StreamTaskInput input = new StreamTaskSourceInput(sourceOperator,index);

        streamInputProcessor = new StreamInputProcessor(input, sourceOutput);
    }

}