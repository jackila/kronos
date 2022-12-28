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

package com.kronos.connector.mysql.source;

import com.kronos.cdc.source.mysql.source.MySqlSource;
import com.kronos.cdc.data.source.DtsRecord;
import com.kronos.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.kronos.cdc.debezium.RowDebeziumDeserializationSchema;
import com.kronos.connector.mysql.testutils.FixationDatabase;
import com.kronos.connector.mysql.testutils.UniqueDatabase;
import com.kronos.jobgraph.JobConfiguration;
import com.kronos.jobgraph.logical.LogicalGraph;
import com.kronos.runtime.jobmaster.JobMaster;
import org.junit.Ignore;
import org.junit.Test;

/** Example Tests for {@link MySqlSource}. */
public class MySqlSourceExampleTest extends MySqlSourceTestBase {

    private UniqueDatabase inventoryDatabase =
            new UniqueDatabase(MYSQL_CONTAINER, "inventory", "mysqluser", "mysqlpw");

    @Test
    @Ignore("Test ignored because it won't stop and is used for manual test")
    public void testConsumingAllEvents() throws Exception {
        inventoryDatabase.createAndInitialize();
        MySqlSource<String> mySqlSource =
                MySqlSource.<String>builder()
                        .hostname(MYSQL_CONTAINER.getHost())
                        .port(MYSQL_CONTAINER.getDatabasePort())
                        .databaseList(inventoryDatabase.getDatabaseName())
                        .tableList(inventoryDatabase.getDatabaseName() + ".products")
                        .username(inventoryDatabase.getUsername())
                        .password(inventoryDatabase.getPassword())
                        .serverId("5401-5404")
                        .deserializer(new JsonDebeziumDeserializationSchema())
                        .includeSchemaChanges(true) // output the schema changes as well
                        .build();

        LogicalGraph graph = new LogicalGraph();
        //graph.build(JobConfiguration.load("example.yml"));
        JobMaster jobMaster = new JobMaster(mySqlSource);
        jobMaster.execute(graph);
    }

    @Test
    @Ignore("Test ignored because it won't stop and is used for manual test")
    public void testConsumingAllEventsInRow() throws Exception {
        inventoryDatabase =
                new FixationDatabase(MYSQL_CONTAINER, "inventory", "mysqluser", "mysqlpw");

        inventoryDatabase.createAndInitialize();
        MySqlSource<DtsRecord> mySqlSource =
                MySqlSource.<DtsRecord>builder()
                        .hostname(MYSQL_CONTAINER.getHost())
                        .port(MYSQL_CONTAINER.getDatabasePort())
                        .databaseList(inventoryDatabase.getDatabaseName())
                        .tableList(inventoryDatabase.getDatabaseName() + ".products",inventoryDatabase.getDatabaseName() + ".orders")
                        .username(inventoryDatabase.getUsername())
                        .password(inventoryDatabase.getPassword())
                        .serverId("5401-5404")
                        .deserializer(new RowDebeziumDeserializationSchema())
                        .includeSchemaChanges(true) // output the schema changes as well
                        .build();

        LogicalGraph graph = new LogicalGraph();
        JobConfiguration config = JobConfiguration.load("order_example.yml");
        config.getSinker().setHost(ES_CONTAINER.getHost()+":"+ES_CONTAINER.getMappedPort(9200));
        graph.build(config);
        JobMaster jobMaster = new JobMaster(mySqlSource);
        jobMaster.execute(graph);
    }
}
