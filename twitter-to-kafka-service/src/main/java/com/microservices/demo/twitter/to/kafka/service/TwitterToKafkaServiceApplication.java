package com.microservices.demo.twitter.to.kafka.service;

import com.microservices.demo.twitter.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;

//This Microservice is used as the Command component part in the CQRS pattern
@SpringBootApplication

@RefreshScope
//Spring Boot applications only read their properties at startup, so property changes made in the Config Server won’t be automatically picked up by the
//    Spring Boot application, but ->
// Spring Boot Actuator offers a @RefreshScope annotation
//    that allows a development team to access a /refresh endpoint that will force the
//    Spring Boot application to reread its application configuration.

@ComponentScan(basePackages = "com.microservices.demo") //Since we will use com.microservices.demo as
// the starting package for all packages in all modules -> Spring will scan and finds ALL the BEANS that resides in ALL MODULES.
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);

  private final StreamRunner streamRunner;

  //private static ApplicationContext applicationContext;

  private final StreamInitializer streamInitializer;

  public TwitterToKafkaServiceApplication(
      StreamRunner runner//, ApplicationContext applicationContext
      , StreamInitializer streamInitializer) {
    this.streamRunner = runner;
    //this.applicationContext = applicationContext;
    this.streamInitializer = streamInitializer;
  }

  public static void main(String[] args) {
    SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    //displayAllBeans();
  }

//  public static void displayAllBeans() {
//    String[] allBeanNames = applicationContext.getBeanDefinitionNames();
//    for (String beanName : allBeanNames) {
//      System.out.println(beanName);
//    }
//  }

//  *Spring Boot will automatically call the run method of all beans implementing the CommandLineRunner interface after the application context has been loaded.*
  @Override
  public void run(String... args) throws Exception {
    LOG.info("App starts...");
    streamInitializer.init(); //the init() will call kafkaAdminClient.createTopics(),
    // and also we are checking if Kafka topics are created and schema registry is up and running before
    //,... before actually starting streaming data from Twitter.
    streamRunner.start();// the start() will cause to create/get the tweets, and then these being sent/streamed
    // as kafka messages;
  }
}
