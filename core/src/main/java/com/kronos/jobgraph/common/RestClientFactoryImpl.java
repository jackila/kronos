package com.kronos.jobgraph.common;

import java.io.Serializable;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;
import org.kronos.RestClientFactory;

/** */
public class RestClientFactoryImpl implements RestClientFactory, Serializable {

    private String username;
    private String password;

    public RestClientFactoryImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void configureRestClientBuilder(RestClientBuilder restClientBuilder) {

        restClientBuilder.setHttpClientConfigCallback(
                new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {

                        // elasticsearch username and password
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(
                                AuthScope.ANY, new UsernamePasswordCredentials(username, password));

                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
    }
}
