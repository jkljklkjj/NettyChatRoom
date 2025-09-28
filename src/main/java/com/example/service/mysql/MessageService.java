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

    public List<Message> getOfflineMessages(String target, int limit) {
        return messageMapper.getOfflineMessages(target, limit);
    }

    @Transactional
    public void markMessagesAsReceived(List<String> ids) {
        messageMapper.markMessagesAsReceived(ids);
    }
}
