package com.microservices.demo.elastic.index.client.util;

import com.microservices.demo.elastic.model.index.IndexModel;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticIndexUtil<T extends IndexModel> {
//  method to return a list of index query from a list of T documents.
//  In this class, we will convert the list of Twitter index model objects to a list of index queries to be able to send them to elasticsearch
//IndexModel interface used here to specify an upper bound for our generic definition.
  //This way, in ElasticIndexUtil, we can use any class that implements Index Model as a replacement for the generic type.
  public List<IndexQuery> getIndexQueries(List<T> documents) {
        return documents.stream()
                .map(document -> new IndexQueryBuilder() //here the document is a upperbounded Class
                        .withId(document.getId()) //document id
                        .withObject(document) //the document
                        .build()
                ).collect(Collectors.toList());
    }
}
