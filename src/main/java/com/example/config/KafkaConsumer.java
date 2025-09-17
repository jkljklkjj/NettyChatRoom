package com.example.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    private ConcurrentMessageListenerContainer<String, String> container;

    public void subscribeToTopic(String topic, String groupId) {
        // 停止并销毁旧的容器
        if (container != null) {
            container.stop();
        }

        // 创建新的容器
        container = kafkaListenerContainerFactory.createContainer(topic);
        container.getContainerProperties().setGroupId(groupId);
        container.getContainerProperties().setMessageListener(new MessageListener<String, String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> record) {
                String targetClientId = record.key();
                String message = record.value();
                System.out.println("收到动态订阅消息，目标用户：" + targetClientId + "，消息内容：" + message);

                // TODO: 将消息推送到客户端或存储到数据库
            }
        });

        // 启动容器
        container.start();
    }
}