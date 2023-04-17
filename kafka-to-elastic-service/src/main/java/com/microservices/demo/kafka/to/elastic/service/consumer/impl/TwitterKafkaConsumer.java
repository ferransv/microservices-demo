package com.microservices.demo.kafka.to.elastic.service.consumer.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaConsumerConfigData;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.to.elastic.service.consumer.KafkaConsumer;
import com.microservices.demo.kafka.to.elastic.service.transformer.AvroToElasticModelTransformer;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

//TwitterKafkaConsumer implements the method receive of our developed KafkaConsumer interface
@Service
public class TwitterKafkaConsumer implements KafkaConsumer<Long, TwitterAvroModel> {

  private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaConsumer.class);

  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  private final KafkaAdminClient kafkaAdminClient;

  private final KafkaConfigData kafkaConfigData;

  private final KafkaConsumerConfigData kafkaConsumerConfigData;

  private final AvroToElasticModelTransformer avroToElasticModelTransformer;

  private final ElasticIndexClient<TwitterIndexModel> elasticIndexClient;

  //Constructor Injection
  public TwitterKafkaConsumer(KafkaListenerEndpointRegistry listenerEndpointRegistry,
                              KafkaAdminClient adminClient,
                              KafkaConfigData configData, KafkaConsumerConfigData kafkaConsumerConfigData,
                              AvroToElasticModelTransformer avroToElasticModelTransformer,
                              ElasticIndexClient<TwitterIndexModel> elasticIndexClient) {
    this.kafkaListenerEndpointRegistry = listenerEndpointRegistry;
    this.kafkaAdminClient = adminClient;
    this.kafkaConfigData = configData;
    this.kafkaConsumerConfigData = kafkaConsumerConfigData;
    this.avroToElasticModelTransformer = avroToElasticModelTransformer;
    this.elasticIndexClient = elasticIndexClient;
  }

  //    We will now add initialization logic to Kafka to elastic
  //The method will take a parameter Application event. -> we will annotate this method with EventListener annotation.
  //Here we will do some checks to be able to ensure that Kafka topics are created.
  @EventListener
  public void onAppStarted(ApplicationStartedEvent event) {
    kafkaAdminClient.checkTopicsCreated();
    LOG.info("Topics with name {} is ready for operations!", kafkaConfigData.getTopicNamesToCreate().toArray());
//  RequireNonNull checks that the specified object reference is not null.
//  This method is designed primarily for doing parameter validation in methods and constructors like in this case, preventing null pointer exc. in the start method
    Objects.requireNonNull(kafkaListenerEndpointRegistry
        .getListenerContainer(kafkaConsumerConfigData.getConsumerGroupId())).start(); //we will get the listener container from the id
    // that we specified in the @KafkaListener annotation, and will START it explicitily
    //As we want to start the kafka consumer EXPLICITILY after checking the Kafka topics, we had changed the value of auto-startup to false
  }

  @Override
  //Replacement used in id and topics for by getting them from the configurations
  //  We have to do it like this, otherwise, this id property will override the Kafka consumer group id,
  //  because there is a property in KafkaListener called idIsGroup which overrides the group id by setting it from the id property
  //And this idIsGroup property is set to true by default
  @KafkaListener(id = "${kafka-consumer-config.consumer-group-id}", topics = "${kafka-config.topic-name}")
  //we have the payload as messages, so we need to annotate this as Payload.
  public void receive(@Payload List<TwitterAvroModel> messages,
                      //Since they rest of the params. are coming from the Kafka headers, we will annotate them with Header
                      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<Integer> keys,
                      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
//        for now, let's just log this info:
    LOG.info("{} number of message/s received with keys {}, partitions {} and offsets {}, " +
            "sending it to elastic: Thread id {}",
        messages.size(),
        keys.toString(),
        partitions.toString(),
        offsets.toString(),
        Thread.currentThread().getId()); //Thread Id
    List<TwitterIndexModel> twitterIndexModels = avroToElasticModelTransformer.getElasticModels(messages);
    List<String> documentIds = elasticIndexClient.save(twitterIndexModels);
    LOG.info("Documents saved to elasticsearch with ids {}", documentIds.toArray());
  }
}
