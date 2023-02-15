package com.microservices.demo.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

//Setting up the Spring Cloud Config bootstrap class

@EnableConfigServer //Enables the service as a Spring Cloud Config service
@SpringBootApplication
public class ConfigServer {
//    main() method that
//    acts as the entry point for the service to start in and a set of Spring annotations that
//    tells the starting service what kind of behaviors Spring is going to launch for the service.
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}