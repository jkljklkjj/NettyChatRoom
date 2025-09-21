package com.example.handler.MessageHandlerImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import com.example.service.redis.RedisService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理登录请求的处理器类
 * 登录信息的接收端是否在线
 */

@Component
public class LoginHandler implements MessageHandler {

    @Autowired
    private RedisService redisService;

    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        // TODO 上线时拉取所有离线收到的消息
        String token = jsonMsg.getString("targetClientId");
        String clientId = redisService.get(token);
        // netty保存session用户id
        ctx.channel().attr(SessionManager.CLIENT_ID_KEY).set(clientId);
        SessionManager.add(clientId, ctx);
    }

}
