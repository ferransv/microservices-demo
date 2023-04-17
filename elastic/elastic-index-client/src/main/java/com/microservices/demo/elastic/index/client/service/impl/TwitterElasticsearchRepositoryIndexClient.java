package com.microservices.demo.elastic.index.client.service.impl;

import com.microservices.demo.elastic.index.client.repository.TwitterElasticsearchIndexRepository;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

//use Primary annotation here, to make this one as the primary implementation of a Elastic Index;
// we use @Primary to give higher preference to a bean when there are multiple beans of the same type. by adding this, we will now have two implementations.
//Because by adding this, we will now have two implementations.
//@Primary
@Service
@ConditionalOnProperty(name = "elastic-config.is-repository", havingValue = "true", matchIfMissing = true)
public class TwitterElasticsearchRepositoryIndexClient implements ElasticIndexClient<TwitterIndexModel> {

  private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticsearchRepositoryIndexClient.class);
  private final TwitterElasticsearchIndexRepository twitterElasticsearchIndexRepository;

  public TwitterElasticsearchRepositoryIndexClient(
      TwitterElasticsearchIndexRepository indexRepository) {
    this.twitterElasticsearchIndexRepository = indexRepository;
  }

  @Override
  public List<String> save(List<TwitterIndexModel> documents) {
    //which will return and Iterable that we can parse to a list of objects again
    List<TwitterIndexModel> repositoryResponse =
        (List<TwitterIndexModel>) twitterElasticsearchIndexRepository.saveAll(documents);
    List<String> docIds = repositoryResponse.stream().map(TwitterIndexModel::getId).collect(Collectors.toList());
    LOG.info("Documents indexed succesfully with type: {} and ids: {}", TwitterIndexModel.class.getName(), docIds);
    return docIds;
  }
}
