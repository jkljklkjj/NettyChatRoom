package com.example.config;

import com.example.constant.KafkaConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String targetClientId, String message) {
        String topic = KafkaConstant.OFFLINE_MESSAGES + targetClientId; // 根据目标用户动态生成主题
        kafkaTemplate.send(topic, targetClientId, message);
    }
}