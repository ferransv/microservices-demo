package com.microservices.demo.elastic.index.client.service.impl;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.index.client.util.ElasticIndexUtil;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

import java.util.List;

// There is an alternative way of querying elasticsearch implemente through TwitterElasticsearchRepositoryIndexClient, which will be the primary one
//    ->using Elasticsearch Repositories allows working with a low level Elasticsearch queries
//   it is a bit more complex than ElasticsearchOperations, but with it you have more control

 //    to implement elastic client by replacing the generic type with TwitterIndexModel class,
 //which is a valid replacement as it's a sub type of IndexModel interface.
@Service
@ConditionalOnProperty(name = "elastic-config.is-repository", havingValue = "false")
public class TwitterElasticIndexClient implements ElasticIndexClient<TwitterIndexModel> {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticIndexClient.class);

    private final ElasticConfigData elasticConfigData;

//     We will use ElasticSearchOperations class to interact with ElasticSearch tu run queries
    private final ElasticsearchOperations elasticsearchOperations;

//     ElasticIndexUtil with the generic type TwitterIndexModel.
    private final ElasticIndexUtil<TwitterIndexModel> elasticIndexUtil;

    public TwitterElasticIndexClient(ElasticConfigData configData,
                                     ElasticsearchOperations elasticOperations,
                                     ElasticIndexUtil<TwitterIndexModel> indexUtil) {
        this.elasticConfigData = configData;
        this.elasticsearchOperations = elasticOperations;
        this.elasticIndexUtil = indexUtil;
    }

    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        //convert the object of list that we have, to a list of index queries
        List<IndexQuery> indexQueries = elasticIndexUtil.getIndexQueries(documents);
        //The bulk index method will save the data in bulk and return a list of String,
        // by sending all queries to ElasticSearch at once by passing the list of queries that we just obtained from the ElasticSearch Index Util, and the index name
        List<String> documentIds = elasticsearchOperations.bulkIndex(
                indexQueries,
                IndexCoordinates.of(elasticConfigData.getIndexName())
        );



//        we will return the list of ids, inserted to the ElasticSearch.
        LOG.info("Documents indexed successfully with type: {} and ids: {}", TwitterIndexModel.class.getName(),
                documentIds);
        return documentIds;
    }
}
