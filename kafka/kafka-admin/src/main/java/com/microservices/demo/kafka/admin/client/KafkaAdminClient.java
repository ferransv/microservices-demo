package com.microservices.demo.kafka.admin.client;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.RetryConfigData;
import com.microservices.demo.kafka.admin.exception.KafkaClientException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class KafkaAdminClient {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final KafkaConfigData kafkaConfigData;

    private final RetryConfigData retryConfigData;

    private final AdminClient adminClient;

    private final RetryTemplate retryTemplate;

    private final WebClient webClient;


    public KafkaAdminClient(KafkaConfigData config,
                            RetryConfigData retryConfigData,
                            AdminClient client,
                            RetryTemplate template,
                            WebClient webClient) {
        this.kafkaConfigData = config;
        this.retryConfigData = retryConfigData;
        this.adminClient = client;
        this.retryTemplate = template;
        this.webClient = webClient;
    }


//  Three methods, create topics, check topics created and check schema registry to
//  be sure that Kafka and Schema Registry is up and running.


  public void createTopics() {
    CreateTopicsResult createTopicsResult;
    try {
      //use retry template execute method to call a new method to create topics
      //Getting the topic names to create and write an info log to mention the  number of topics that are being created.
      createTopicsResult = retryTemplate.execute(this::doCreateTopics);
      LOG.info("Create topic result {}", createTopicsResult.values().values());
    } catch (Throwable t) {
      throw new KafkaClientException("Reached max number of retry for creating kafka topic(s)!", t);
    }
    //to double check to be sure topics are created
    checkTopicsCreated();
  }
  //Here it might take some time to see the created topics once we called the get topics method
  //!because the creation of topics is an ASYNCHRONOUS operation.
  public void checkTopicsCreated() {
    Collection<TopicListing> topics = getTopics();
    int retryCount = 1;
    Integer maxRetry = retryConfigData.getMaxAttempts();
    int multiplier = retryConfigData.getMultiplier().intValue();
    Long sleepTimeMs = retryConfigData.getSleepTimeMs();
    //we will go over all the topic names that are to be created, to check if the topic is created or not
    for (String topic : kafkaConfigData.getTopicNamesToCreate()) {
      while (!isTopicCreated(topics, topic)) {

        checkMaxRetry(retryCount++, maxRetry);
        sleep(sleepTimeMs);
        sleepTimeMs *= multiplier;
        topics = getTopics();
      }
    }
  }

  //to check if schema registry is up and running, because we will don't want it to fail at startup, when we run everything,
  // including schema registry, Kafka and our services in the same compose file
  public void checkSchemaRegistry() {
    int retryCount = 1;
    Integer maxRetry = retryConfigData.getMaxAttempts();
    int multiplier = retryConfigData.getMultiplier().intValue();
    Long sleepTimeMs = retryConfigData.getSleepTimeMs();
    //We have to do a REST call to the schema Registry endpoints using webclient webflux
    //Web client is the non blocking the reactive client to perform HTTP requests, and it provides a fluent API
    while (!getSchemaRegistryStatus().is2xxSuccessful()) {
      //I will wait until schema registry is up and running or the retry count reaches to the maximum amount
      checkMaxRetry(retryCount++, maxRetry);
      sleep(sleepTimeMs);
      sleepTimeMs *= multiplier;
    }
  }

  private HttpStatus getSchemaRegistryStatus() {
    try {
      //instead of creating the Web client in my code each time, I want to create a spring bean and
      //Reuse it each time when I need it a web client.
      //-> trough configuration class with @Bean annotation
      return webClient
          .method(HttpMethod.GET)
          .uri(kafkaConfigData.getSchemaRegistryUrl())
          .exchange()
          .map(ClientResponse::statusCode) //map the response to a client response status code object.
          .block(); //finally block the operation to be able to get the results SYCHRONOUSLY from schema registry.
    } catch (Exception e) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    }
  }

  private void sleep(Long sleepTimeMs) {
    try {
      Thread.sleep(sleepTimeMs);
    } catch (InterruptedException e) {
      throw new KafkaClientException("Error while sleeping for waiting new created topics!!");
    }
  }

  //we will throw a Kafka client exception if max retry has been reached
  private void checkMaxRetry(int retry, Integer maxRetry) {
    if (retry > maxRetry) {
      throw new KafkaClientException("Reached max number of retry for reading kafka topic(s)!");
    }
  }

  private boolean isTopicCreated(Collection<TopicListing> topics, String topicName) {
      if (topics == null) {
      return false;
    }
    // we will convert the topics to a stream and search for a match for the topic name
    return topics.stream().anyMatch(topic -> topic.name().equals(topicName));
  }

  private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
    List<String> topicNames = kafkaConfigData.getTopicNamesToCreate();
    LOG.info("Creating {} topics(s), attempt {}", topicNames.size(), retryContext.getRetryCount());
    //we will convert to list of topic names to stream, and then map it to a new topic class
    List<NewTopic> kafkaTopics = topicNames.stream().map(topic ->
        //In order to create a new topic object we will pass topic name as parameter
        //As well as number of partitions and replication factor by reading them from Kafka config data object.
        new NewTopic(
            topic.trim(),
            kafkaConfigData.getNumOfPartitions(),
            kafkaConfigData.getReplicationFactor()
        )).collect(Collectors.toList());
    //Topics factual creation. It is an asynchronous operation
    return adminClient.createTopics(kafkaTopics);
  }

  private Collection<TopicListing> getTopics() {
    Collection<TopicListing> topics;
    try {
      topics = retryTemplate.execute(this::doGetTopics);
    } catch (Throwable t) {
      //throw an exception in case retry template failed with a throwable.
      throw new KafkaClientException("Reached max number of retry for reading kafka topic(s)!", t);
    }
    return topics;
  }

  private Collection<TopicListing> doGetTopics(RetryContext retryContext)
      throws ExecutionException, InterruptedException {
    LOG.info("Reading kafka topic {}, attempt {}",
        kafkaConfigData.getTopicNamesToCreate().toArray(), retryContext.getRetryCount());
    Collection<TopicListing> topics = adminClient.listTopics().listings().get();
    if (topics != null) {
      topics.forEach(topic -> LOG.debug("Topic with name {}", topic.name()));
    }
    return topics;
  }

}
