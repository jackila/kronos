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

package com.kronos.cdc.data;

import io.debezium.data.Envelope.Operation;

/** Lists all kinds of changes that a row can describe in a changelog. */
public enum RowKind {

    // Note: Enums have no stable hash code across different JVMs, use toByteValue() for
    // this purpose.

    /** Insertion operation. */
    INSERT("I", (byte) 0),

    /**
     * Update operation
     *
     */
    UPDATE("U", (byte) 1),

    /** Deletion operation. */
    DELETE("D", (byte) 2);

    private final String shortString;

    private final byte value;

    /**
     * Creates a {@link RowKind} enum with the given short string and byte value representation of
     * the {@link RowKind}.
     */
    RowKind(String shortString, byte value) {
        this.shortString = shortString;
        this.value = value;
    }

    /**
     * Returns a short string representation of this {@link RowKind}.
     *
     * <p>
     *
     * <ul>
     *   <li>"+I" represents {@link #INSERT}.
     *   <li>"-U" represents {@link #UPDATE}.
     *   <li>"-D" represents {@link #DELETE}.
     * </ul>
     */
    public String shortString() {
        return shortString;
    }

    /**
     * Returns the byte value representation of this {@link RowKind}. The byte value is used for
     * serialization and deserialization.
     *
     * <p>
     *
     * <ul>
     *   <li>"0" represents {@link #INSERT}.
     *   <li>"1" represents {@link #UPDATE}.
     *   <li>"2" represents {@link #DELETE}.
     * </ul>
     */
    public byte toByteValue() {
        return value;
    }

    /**
     * Creates a {@link RowKind} from the given byte value. Each {@link RowKind} has a byte value
     * representation.
     *
     * @see #toByteValue() for mapping of byte value and {@link RowKind}.
     */
    public static RowKind fromByteValue(Operation value) {
        switch (value) {
            case CREATE:
            case READ:
                return INSERT;
            case UPDATE:
                return UPDATE;
            case DELETE:
                return DELETE;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported byte value '" + value + "' for row kind.");
        }
    }
}
