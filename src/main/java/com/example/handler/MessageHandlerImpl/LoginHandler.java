package com.example.handler.MessageHandlerImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.stereotype.Component;

/**
 * 处理登录请求的处理器类
 * 登录信息的接收端是否在线
 */

@Component
public class LoginHandler implements MessageHandler {
    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        /*
         * 要求信息：
         * clientId: 通过token和redis获取的客户端的ID
         * token需要通过header获取
         */
        // TODO 上线时拉取所有离线收到的消息
        String clientId = jsonMsg.getString("UserId");
        SessionManager.add(clientId, ctx);
    }

}
