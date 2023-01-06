package com.kronos.jobgraph.physic.disruptor;

import com.google.common.collect.Lists;
import com.lmax.disruptor.Sequence;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** */
public class ProcessorInput {
    private Optional<List<Sequence>> source;

    public boolean isEmpty() {
        return source == null || !source.isPresent() || source.get().isEmpty();
    }

    public ProcessorInput(Sequence source) {
        this.source = Optional.of(Lists.newArrayList(source));
    }

    public ProcessorInput() {
        source = Optional.of(new ArrayList<>());
    }

    public Sequence[] getSource() {
        return source.orElseThrow(() -> new RuntimeException("source need a sequence"))
                .toArray(new Sequence[0]);
    }

    public void setSource(Optional<List<Sequence>> source) {
        this.source = source;
    }

    public ProcessorInput addSource(Sequence output) {
        source.get().add(output);
        return this;
    }

    public int size() {
        if (isEmpty()) {
            return 0;
        }
        return source.get().size();
    }

    public void addSource(ProcessorInput nextInput) {
        source.get().addAll(nextInput.source.get());
    }
}
