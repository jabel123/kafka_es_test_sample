package com.example.estest;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

class EsTest
{
    private static RestHighLevelClient client;

    @BeforeAll
    public static void beforeEach()
    {
        // docker pull
        ElasticsearchContainer container
            = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.12.0");

        // container start
        container.start();

        // RestHighLevelClient을 이용하기 위한 접근 설정
        BasicCredentialsProvider credentialProvider = new BasicCredentialsProvider();
        credentialProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("elasticsearch", "elasticsearch"));

        RestClientBuilder builder = RestClient.builder(HttpHost.create(container.getHttpHostAddress()))
            .setHttpClientConfigCallback(
                httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialProvider)
            );

        client = new RestHighLevelClient(builder);
    }

    @BeforeEach
    public void initData() throws IOException
    {
        client.indices().delete(new DeleteIndexRequest("*"), RequestOptions.DEFAULT);
    }

    @Test
    @DisplayName("인덱스가 있는지 확인하는 테스트")
    public void indexCreateTest() throws IOException
    {
        String indexName = "test_index";
        IndicesClient indices = client.indices();
        boolean exists = indices.exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);

        assertThat(exists).isFalse();

        CreateIndexRequest request = new CreateIndexRequest(indexName);
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    @DisplayName("인덱스 넣기 테스트")
    public void dataInsertToIndexTest() throws IOException
    {
        IndexRequest indexRequest = new IndexRequest("test_index");

        indexRequest.source(jsonBuilder().startObject()
            .field("name", "주현태")
            .field("age", 32)
            .field("hobby", "sleep")
            .endObject()
        );

        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        assertThat(indexResponse.status()).isEqualTo(RestStatus.CREATED);
    }
}
