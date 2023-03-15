package com.microservices.demo.kafka.admin.config;

import com.microservices.demo.config.KafkaConfigData;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Map;

@EnableRetry //To enable Spring Retry in an application,
             // we need to add the @EnableRetry annotation to our @Configuration class
@Configuration
public class KafkaAdminConfig {

    private final KafkaConfigData kafkaConfigData;

    public KafkaAdminConfig(KafkaConfigData configData) {
        this.kafkaConfigData = configData;
    }


//    About the use of @Bean vs @Component:
//    Sometimes automatic configuration is not an option. When? Let's imagine that you want to wire components
//    from 3rd-party libraries (you don't have the source code so you can't annotate its classes with @Component),
//    so automatic configuration is not possible.
//    The @Bean annotation returns an object that spring should register as bean in application context.
//    The body of the method bears the logic responsible for creating the instance.
//
    //AdminClient.create will create the AdminClient of the (docker) bootstrap-servers: localhost:19092, localhost:29092, localhost:39092
    // to then be able aftwerwards to create and list the Kafka TOPICS programmatically
    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                kafkaConfigData.getBootstrapServers()));
    }
}
