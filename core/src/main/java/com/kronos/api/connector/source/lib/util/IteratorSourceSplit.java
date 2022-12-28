package com.kronos.api.connector.source.lib.util;

import com.kronos.api.connector.source.SourceSplit;

import java.util.Iterator;

/**
 * @Author: jackila
 * @Date: 13:45 2022-9-17
 */
public interface IteratorSourceSplit extends SourceSplit {
    /** Gets the iterator over the elements of this split. */
    Iterator getIterator();
}
