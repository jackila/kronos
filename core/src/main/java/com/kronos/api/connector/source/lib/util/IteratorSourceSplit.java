package com.kronos.api.connector.source.lib.util;

import com.kronos.api.connector.source.SourceSplit;
import java.util.Iterator;

/** */
public interface IteratorSourceSplit extends SourceSplit {
    /** Gets the iterator over the elements of this split. */
    Iterator getIterator();
}
