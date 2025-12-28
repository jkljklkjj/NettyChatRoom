package com.example.controller;

import com.example.annotation.RequireUserId;
import com.example.common.api.ApiResponse;
import com.example.common.api.StatusCode;
import com.example.model.mysql.Message;
import com.example.service.mysql.MessageService;
import com.example.service.redis.RedisOfflineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    MessageService messageService;
    @Autowired
    RedisOfflineService redisOfflineService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @RequireUserId
    @RequestMapping("/getPersistedOfflineMessage")
    public ApiResponse<List<Message>> getPersistedOfflineMessage(@RequestAttribute(value = "UserId", required = false) String userId) {
        int limit = 10;
        List<Message> offlineMessages = messageService.getOfflineMessages(userId, limit);
        if (!offlineMessages.isEmpty()) {
            List<String> ids = offlineMessages.stream().map(Message::getId).collect(Collectors.toList());
            messageService.markMessagesAsReceived(ids);
        }
        System.out.println(offlineMessages);
        return ApiResponse.success(offlineMessages);
    }

    @RequireUserId
    @RequestMapping("/getOfflineMessage")
    public ApiResponse<List<Message>> getOfflineMessage(
            @RequestAttribute(value = "UserId", required = false) String userId,
            @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", required = false) Integer pageSize) {

        int limit = (pageSize == null || pageSize <= 0) ? 10 : pageSize;
        List<Message> messages = redisOfflineService.fetchAndClearPaginated(userId, limit);
        // 批量入库
        if (messages != null && !messages.isEmpty()) {
            messageService.insertMessages(messages);
        }
        ApiResponse<List<Message>> response = ApiResponse.success(messages == null ? Collections.emptyList() : messages);
        if(messages.size() == limit){
            response.setCode(StatusCode.PARTICIAL);
        }
        return response;
    }
}