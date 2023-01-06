package com.kronos.api.connector.source.lib;

import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.api.connector.source.SplitEnumeratorContext;
import com.kronos.api.connector.source.lib.util.IteratorSourceEnumerator;
import com.kronos.api.connector.source.lib.util.IteratorSourceReader;
import com.kronos.api.connector.source.lib.util.IteratorSourceSplit;
import com.kronos.api.connector.source.lib.util.NumberSequenceIterator;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.jobgraph.physic.operator.source.SourceReader;
import com.kronos.jobgraph.physic.operator.source.SourceReaderContext;
import java.util.ArrayList;
import java.util.List;

/** 一个产生有序数字的数据源 */
public class NumberSequenceSource implements Source {

    private final long from;

    private final long to;

    public NumberSequenceSource(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public SourceReader createSourceReader(SourceReaderContext readerContext) {
        return new IteratorSourceReader(readerContext);
    }

    @Override
    public SplitEnumerator createEnumerator(SplitEnumeratorContext enumContext) {

        final List<IteratorSourceSplit> splits =
                splitNumberRange(from, to, enumContext.currentParallelism());
        IteratorSourceEnumerator iteratorSourceEnumerator =
                new IteratorSourceEnumerator(enumContext, splits);
        return iteratorSourceEnumerator;
    }

    protected List<IteratorSourceSplit> splitNumberRange(long from, long to, int numSplits) {
        final NumberSequenceIterator[] subSequences =
                new NumberSequenceIterator(from, to).split(numSplits);
        final ArrayList<IteratorSourceSplit> splits = new ArrayList<>(subSequences.length);

        int splitId = 1;
        for (NumberSequenceIterator seq : subSequences) {
            if (seq.hasNext()) {
                splits.add(
                        new NumberSequenceSplit(
                                String.valueOf(splitId++), seq.getCurrent(), seq.getTo()));
            }
        }

        return splits;
    }

    /** A split of the source, representing a number sub-sequence. */
    public static class NumberSequenceSplit implements IteratorSourceSplit {

        private final String splitId;
        private final long from;
        private final long to;

        public NumberSequenceSplit(String splitId, long from, long to) {
            this.splitId = splitId;
            this.from = from;
            this.to = to;
        }

        @Override
        public String splitId() {
            return splitId;
        }

        public long from() {
            return from;
        }

        public long to() {
            return to;
        }

        @Override
        public NumberSequenceIterator getIterator() {
            return new NumberSequenceIterator(from, to);
        }

        @Override
        public String toString() {
            return String.format("NumberSequenceSplit [%d, %d] (%s)", from, to, splitId);
        }
    }
}
