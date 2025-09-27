package com.example.config;

import com.example.constant.KafkaConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String targetClientId, String message) {
        // 所有用户共用同一个topic，key为targetClientId或者随机值
        String topic = KafkaConstant.OFFLINE_MESSAGES;
//        kafkaTemplate.send(topic, targetClientId, message);
        String random = UUID.randomUUID().toString();
        kafkaTemplate.send(topic, random, message);
    }
}