package com.kronos.jobgraph.physic.disruptor;

import com.lmax.disruptor.Sequence;

import java.util.Optional;

/**
 * @Author: jackila
 * @Date: 11:15 2022-12-15
 */
public class ProcessorOutput {

    private Optional<Sequence> out;

    public ProcessorOutput(Sequence out) {
        this.out = Optional.of(out);
    }

    public ProcessorOutput() {
        out = Optional.empty();
    }

    public Optional<Sequence> getOut() {
        return out;
    }
    public Sequence output() {
        return out.orElseThrow(()->new RuntimeException("out should have a value"));
    }

    public Sequence[] outArray() {
        return new Sequence[]{out.orElseThrow(()->new RuntimeException("out should have a value"))};
    }

    public void setOut(Optional<Sequence> out) {
        this.out = out;
    }

    public ProcessorInput convertTo(){
        return new ProcessorInput(out.orElseThrow(()->new RuntimeException("out should have a value")));
    }
}
