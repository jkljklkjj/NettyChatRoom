package com.example.controller;

import com.example.annotation.RequireUserId;
import com.example.common.api.ApiResponse;
import com.example.model.mysql.Message;
import com.example.service.mysql.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    MessageService messageService;

    @RequireUserId
    @RequestMapping("/getOfflineMessage")
    public ApiResponse<List<Message>> getOfflineMessage(@RequestAttribute(value = "UserId", required = false) String userId) {
        int limit = 10;
        List<Message> offlineMessages = messageService.getOfflineMessages(userId, limit);
        if (!offlineMessages.isEmpty()) {
            List<String> ids = offlineMessages.stream().map(Message::getSenderId).collect(Collectors.toList());
            messageService.markMessagesAsReceived(ids);
        }
        System.out.println(offlineMessages);
        return ApiResponse.success(offlineMessages);
    }
}