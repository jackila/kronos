/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.cdc.source.base.source.reader;

import com.kronos.cdc.source.base.source.reader.splitreader.SourceReaderBase;
import com.kronos.config.ConfigOption;
import com.kronos.config.ConfigOptions;
import com.kronos.config.Configuration;

/** The options tht can be set for the {@link SourceReaderBase}. */
public class SourceReaderOptions {

    public static final ConfigOption<Long> SOURCE_READER_CLOSE_TIMEOUT =
            ConfigOptions.key("source.reader.close.timeout")
                    .longType()
                    .defaultValue(30000L)
                    .withDescription("The timeout when closing the source reader");

    public static final ConfigOption<Integer> ELEMENT_QUEUE_CAPACITY =
            ConfigOptions.key("source.reader.element.queue.capacity")
                    .intType()
                    .defaultValue(2)
                    .withDescription("The capacity of the element queue in the source reader.");

    // --------------- final fields ----------------------
    public final long sourceReaderCloseTimeout;
    public final int elementQueueCapacity;

    public SourceReaderOptions(Configuration config) {
        this.sourceReaderCloseTimeout = SOURCE_READER_CLOSE_TIMEOUT.defaultValue();
        this.elementQueueCapacity = ELEMENT_QUEUE_CAPACITY.defaultValue();
    }
}
