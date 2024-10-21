package com.example.handler.MessageHandlerImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.StringMessageHandler;

import io.netty.channel.ChannelHandlerContext;

public class LoginHandler implements MessageHandler {
    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        String clientId = jsonMsg.getString("clientId");
        StringMessageHandler.add(clientId, ctx);
    }
    
}