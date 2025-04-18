package com.example.handler.MessageHandlerImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.channel.ChannelHandlerContext;

/**
 * 处理登出请求的处理器类
 */
public class LogoutHandler implements MessageHandler {
    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        /*
         * 要求信息：
         * clientId: 通过token和redis获取的客户端的ID
         * token需要通过header获取
         */
        String clientId = jsonMsg.getString("UserId");
        SessionManager.remove(clientId);
    }
    
}
