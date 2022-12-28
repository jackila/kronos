/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.cdc.data.sink.es;

/**
 * Configuration Keys for EsDataWriter and EsDataReader
 * <p>
 * Company: www.dtstack.com
 *
 * @author huyifan.zju@163.com
 */
public class EsConfigKeys {

    public static final String KEY_ADDRESS = "address";

    public static final String KEY_HOST = "host";

    public static final String KEY_PORT = "port";

    public static final String KEY_USERNAME = "username";

    public static final String KEY_PASSWORD = "password";

    public static final String KEY_QUERY = "query";

    public static final String KEY_INDEX = "index";

    public static final String KEY_NAME_TYPE = "nameType";

    public static final String KEY_UPSERT = "upsertTime";

    public static final String KEY_TYPE = "type";

    public static final String KEY_BULK_SIZE_MB = "bulkSizeMB";

    public static final String KEY_BULK_ACTION = "bulkAction";

    public static final String KEY_BULK_INTERVAL_MS = "bulkIntervalMs";

    public static final String BACKOFF_ENABLE = "backoffEnable";

    public static final String BACKOFF_TYPE = "backoffType";

    public static final String BACKOFF_DELAY = "backoffDelay";

    public static final String BACKOFF_RETRIES = "backoffRetries";

    public static final String KEY_COLUMN_NAME = "name";

    public static final String KEY_COLUMN_TYPE = "type";

    public static final String KEY_COLUMN_FORMAT = "format";

    public static final String KEY_COLUMN_TO_FORMAT = "toFormat";

    public static final String KEY_COLUMN_PRECISION = "precision";

    public static final String KEY_COLUMN_ARRAY = "array";

    public static final String KEY_COLUMN_SOURCE_TABLE = "sourceTable";

    public static final String KEY_COLUMN_SOURCE_FIELD = "sourceField";

    public static final String KEY_COLUMN_NESTED_TABLE_COLUMNS = "nestedTableColumns";

    public static final String KEY_COLUMN_ASSOCIATION = "association";

    public static final String KEY_ID_COLUMN = "idColumn";

    public static final String KEY_ID_COLUMN_INDEX = "index";

    public static final String KEY_ID_COLUMN_TYPE = "type";

    public static final String KEY_ID_COLUMN_VALUE = "value";

    public static final String KEY_TIMEOUT = "timeout";

    public static final String KEY_PATH_PREFIX = "pathPrefix";

    public static final String KEY_DEFAULT_TABLE = "defaultTable";

    public static final String KEY_RELATIONSHIP = "relationship";

    public static final String ONE_TO_ONE = "OneToOne";

    public static final String ONE_TO_MANY = "OneToMany";

}
