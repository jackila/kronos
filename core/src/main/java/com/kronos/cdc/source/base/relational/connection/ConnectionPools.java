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

package com.kronos.cdc.source.base.relational.connection;

import com.kronos.cdc.source.base.config.SourceConfig;
import com.kronos.cdc.source.mysql.source.connection.ConnectionPoolId;

/** A pool collection that consists of multiple connection pools. */
public interface ConnectionPools<P, C extends SourceConfig> {

    /**
     * Gets a connection pool from pools, create a new pool if the pool does not exists in the
     * connection pools .
     */
    P getOrCreateConnectionPool(ConnectionPoolId poolId, C sourceConfig);
}