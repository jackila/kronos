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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kronos.cdc.source.mysql.source.MySqlSource;
import com.kronos.connector.mysql.testutils.ElasticsearchVersion;
import com.kronos.connector.mysql.testutils.MySqlContainer;
import com.kronos.connector.mysql.testutils.MySqlVersion;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.lifecycle.Startables;

/** Basic class for testing {@link MySqlSource}. */
public abstract class MySqlSourceTestBase {

    protected static final Logger LOG = LoggerFactory.getLogger(MySqlSourceTestBase.class);

    protected static final int DEFAULT_PARALLELISM = 4;
    /** Elasticsearch default username, when secured */
    private static final String ELASTICSEARCH_USERNAME = "elastic";
    /** From 6.8, we can optionally activate security with a default password. */
    private static final String ELASTICSEARCH_PASSWORD = "123456";

    protected static final MySqlContainer MYSQL_CONTAINER = createMySqlContainer(MySqlVersion.V5_7);
    protected static final ElasticsearchContainer ES_CONTAINER =
            createElasticsearchContainer(ElasticsearchVersion.V7_9_2);

    @BeforeClass
    public static void startContainers() {
        LOG.info("Starting containers...");
        Startables.deepStart(Stream.of(MYSQL_CONTAINER, ES_CONTAINER)).join();
        LOG.info("Containers are started.");
        init();
    }

    @AfterClass
    public static void stopContainers() {
        LOG.info("Stopping containers...");
        MYSQL_CONTAINER.stop();
        ES_CONTAINER.stop();
        LOG.info("Containers are stopped.");
    }

    @SneakyThrows
    public static void init() {
        // Do whatever you want with the rest client ...
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD));

        RestClient client =
                RestClient.builder(HttpHost.create(ES_CONTAINER.getHttpHostAddress()))
                        .setHttpClientConfigCallback(
                                httpClientBuilder -> {
                                    return httpClientBuilder.setDefaultCredentialsProvider(
                                            credentialsProvider);
                                })
                        .build();

        Response response = client.performRequest(new Request("GET", "/_cluster/health"));

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertTrue(EntityUtils.toString(response.getEntity()).contains("cluster_name"));

        Request createIndex = new Request("PUT", "/wide_orders");
        Response indexResponse = client.performRequest(createIndex);
        assertEquals(indexResponse.getStatusLine().getStatusCode(), 200);
    }

    protected static MySqlContainer createMySqlContainer(MySqlVersion version) {
        return (MySqlContainer)
                new MySqlContainer(version)
                        .withConfigurationOverride("docker/server-gtids/my.cnf")
                        .withSetupSQL("docker/setup.sql")
                        .withDatabaseName("flink-test")
                        .withUsername("flinkuser")
                        .withPassword("flinkpw")
                        .withLogConsumer(new Slf4jLogConsumer(LOG));
    }

    protected static ElasticsearchContainer createElasticsearchContainer(
            ElasticsearchVersion version) {
        ElasticsearchContainer container =
                new ElasticsearchContainer().withPassword(ELASTICSEARCH_PASSWORD);
        container.addEnv("action.auto_create_index", "*");
        return container;
    }

    public static void assertEqualsInAnyOrder(List<String> expected, List<String> actual) {
        assertTrue(expected != null && actual != null);
        assertEqualsInOrder(
                expected.stream().sorted().collect(Collectors.toList()),
                actual.stream().sorted().collect(Collectors.toList()));
    }

    public static void assertEqualsInOrder(List<String> expected, List<String> actual) {
        assertTrue(expected != null && actual != null);
        assertEquals(expected.size(), actual.size());
        assertArrayEquals(expected.toArray(new String[0]), actual.toArray(new String[0]));
    }
}
