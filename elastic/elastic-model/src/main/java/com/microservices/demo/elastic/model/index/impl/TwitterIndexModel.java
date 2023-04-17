package com.microservices.demo.elastic.model.index.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservices.demo.elastic.model.index.IndexModel;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data //I want to use Builder and Data annotations from Lombok to be able to easily work on it.
@Builder
//@Document Indicates that this class is a candidate for mapping to elasticsearch
@Document(indexName = "#{elasticConfigData.indexName}") //For this annotation, we need to set index name.
// And to set it, we will use spring expression language, to get the name defined in the configuration.
//Index name is parsed using Spel expression parser class, so that we need to use expression language here
public class TwitterIndexModel implements IndexModel {

  @JsonProperty
  private String id;
  @JsonProperty
  private Long userId;
  @JsonProperty
  private String text;

  //    For this field, we need to define 2 additional annotations apart from JsonProperty
  //    (for both of them, we use the same pattern):

  //This is an annotation in Spring library to be able to convert createdAt,
  // from local date time to the elasticsearch date DURING INDEXING operation.
  //Here we used a custom date format because we get a date from twitter status object in a custom format
  @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ")
  //specificy a Date pattern and a shape format.
  // For the parsing it formats the field when converting object to json by using the pattern specified
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ")
  @JsonProperty
  private LocalDateTime createdAt;

  //We can delete this because lombok (@Data) will generate the setter and the getters to the compiled class
//  @Override
//  public String getId(){
//    return id;
//  }

}
