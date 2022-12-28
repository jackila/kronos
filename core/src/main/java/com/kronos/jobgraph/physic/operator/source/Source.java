package com.kronos.jobgraph.physic.operator.source;


import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.api.connector.source.SplitEnumeratorContext;

/**
 * @Author: jackila
 * @Date: 11:41 AM 2022-8-01
 */
public interface Source {

    SourceReader createSourceReader(SourceReaderContext context);

    SplitEnumerator createEnumerator(SplitEnumeratorContext context);
}
