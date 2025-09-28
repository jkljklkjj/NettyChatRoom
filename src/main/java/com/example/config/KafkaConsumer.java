package com.example.config;

import com.example.constant.KafkaConstant;
import com.example.service.mysql.MessageService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.example.model.mysql.Message;
import com.alibaba.fastjson.JSON;

@Service
public class KafkaConsumer {
    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = KafkaConstant.OFFLINE_MESSAGES)
    public void listen(ConsumerRecord<String, String> record) {
        String json = record.value();
        Message message = JSON.parseObject(json, Message.class);
        messageService.insertMessage(message);
    }
}