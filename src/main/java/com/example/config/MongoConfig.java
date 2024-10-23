package com.example.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettings mongoClientSettings() {
    return MongoClientSettings.builder()
        .applyToClusterSettings(builder ->
            builder.hosts(Collections.singletonList(new ServerAddress("39.104.61.4", 27017))))
        .applyToConnectionPoolSettings(builder ->
            builder.maxSize(100)
                .minSize(0)
                .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                .maxWaitTime(120, TimeUnit.SECONDS))
        .applyToSocketSettings(builder ->
            builder.connectTimeout(1000, TimeUnit.MILLISECONDS)
                .readTimeout(1500, TimeUnit.MILLISECONDS))
        .build();
    }

    @Bean
    public MongoClient mongoClient(MongoClientSettings settings) {
        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, "test"));
    }
}