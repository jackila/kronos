/*
 * Copyright 2022 Ververica Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.cdc.debezium;

import com.kronos.api.operators.Collector;
import java.io.Serializable;
import org.apache.kafka.connect.source.SourceRecord;

/**
 * The deserialization schema describes how to turn the Debezium SourceRecord into data types
 * (Java/Scala objects) that are processed by Flink.
 *
 * @param <T> The type created by the deserialization schema.
 */
public interface DebeziumDeserializationSchema<T> extends Serializable {

    /** Deserialize the Debezium record, it is represented in Kafka {@link SourceRecord}. */
    void deserialize(SourceRecord record, Collector<T> out) throws Exception;
}
