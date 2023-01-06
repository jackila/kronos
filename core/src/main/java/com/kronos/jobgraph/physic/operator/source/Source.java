package com.kronos.jobgraph.physic.operator.source;

import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.api.connector.source.SplitEnumeratorContext;

/** */
public interface Source {

    SourceReader createSourceReader(SourceReaderContext context);

    SplitEnumerator createEnumerator(SplitEnumeratorContext context);
}
