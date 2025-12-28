package com.example.service.mysql;

import com.example.mapper.MessageMapper;
import com.example.model.mysql.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Transactional
    public void insertMessage(Message message) {
        messageMapper.insertMessage(message);
    }

    @Transactional
    public void insertMessages(java.util.List<Message> messages) {
        if (messages == null || messages.isEmpty()) return;
        messageMapper.insertMessages(messages);
    }

    /**
     * 从持久化存储读取离线消息（按 receiver id, limit）
     */
    public List<Message> getOfflineMessages(String receiverId, int limit) {
        return messageMapper.getOfflineMessages(receiverId, limit);
    }

    @Transactional
    public void markMessagesAsReceived(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        messageMapper.markMessagesAsReceived(ids);
    }
}
