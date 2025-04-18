//package com.example.config;
//import io.lettuce.core.ClientOptions;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public ClientOptions clientOptions() {
//        return ClientOptions.builder()
//                .autoClientSetInfo(false) // Disables CLIENT SETINFO
//                .build();
//    }
//}