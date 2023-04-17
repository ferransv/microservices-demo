package com.microservices.demo.kafka.to.elastic.service.transformer;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

//We will use this class to convert Avro model to Elastic model, as we need to obtain a model that fits to Elasticsearch
@Component
public class AvroToElasticModelTransformer {

  public List<TwitterIndexModel> getElasticModels(List<TwitterAvroModel> avroModels) {
    return avroModels.stream()
        .map(avroModel -> TwitterIndexModel
            .builder()
            .userId(avroModel.getUserId())
            .id(String.valueOf(avroModel.getId()))
            .text(avroModel.getText())
            //from Long to LocalDateTime
            .createdAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(avroModel.getCreatedAt()),
                ZoneId.systemDefault()))
            .build()
        ).collect(Collectors.toList());
  }
}

