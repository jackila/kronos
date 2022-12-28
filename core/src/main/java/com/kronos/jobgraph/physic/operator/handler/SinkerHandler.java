package com.kronos.jobgraph.physic.operator.handler;

import com.kronos.cdc.data.DiffStageRecords;
import com.kronos.cdc.data.sink.RecordSet;
import com.kronos.jobgraph.physic.StreamRecord;
import lombok.SneakyThrows;
import org.kronos.connector.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: jackila
 * @Date: 19:17 2022-12-16
 */
public class SinkerHandler extends AbstractTableTransformerHandler<StreamRecord<DiffStageRecords>> {

    private Logger logger = LoggerFactory.getLogger(SinkerHandler.class);

    private SinkFunction<RecordSet> sinker;

    public SinkerHandler() {
    }

    @SneakyThrows
    public SinkerHandler(SinkFunction sink) {
        this.sinker = sink;
        this.sinker.open();
    }

    @Override
    @SneakyThrows
    public void catchEventChange(StreamRecord<DiffStageRecords> event) {
        sinker.invoke(new RecordSet(event.value()));
    }
}
