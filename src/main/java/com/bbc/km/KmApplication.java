package com.bbc.km;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableMongoRepositories
@EnableScheduling
@EnableMongoAuditing
@SpringBootApplication
public class KmApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmApplication.class, args);
    }

}
