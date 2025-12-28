package com.example.config;

import com.example.constant.KafkaConstant;
import com.example.service.mysql.MessageService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.example.model.mysql.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = KafkaConstant.MESSAGES)
    public void listen(ConsumerRecord<String, String> record) {
        String json = record == null ? null : record.value();
        if (json == null || json.trim().isEmpty()) return;

        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("[")) {
                try {
                    List<Message> messages = JSONArray.parseArray(trimmed, Message.class);
                    if (messages != null && !messages.isEmpty()) {
                        messageService.insertMessages(messages);
                    }
                } catch (Exception parseEx) {
                    // 如果解析失败，记录并继续尝试解析为单个对象
                    logger.error("Failed to parse Kafka message as JSON array, will try single object. payload={}", trimmed, parseEx);
                }
                return;
            }

            // 单个对象兼容处理
            Message message = JSON.parseObject(trimmed, Message.class);
            if (message != null) {
                messageService.insertMessage(message);
            }
        } catch (Exception e) {
            // 记录异常，避免 Kafka listener 无法继续
                logger.error("Unexpected error when processing Kafka record: topic={} partition={} offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
        }
    }
}