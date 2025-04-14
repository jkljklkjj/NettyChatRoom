package com.example.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    // key: 用户Id value: 通道上下文
    private static final Map<String, ChannelHandlerContext> clientChannels = new ConcurrentHashMap<>();
    // 新增channel的信息
    private static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf("clientId");

    public SessionManager() {
    }

    public static ChannelHandlerContext get(String clientId) {
        return clientChannels.get(clientId);
    }

    public static void add(String clientId, ChannelHandlerContext ctx) {
        ctx.channel().attr(CLIENT_ID_KEY).set(clientId);
        clientChannels.put(clientId, ctx);
    }

    public static void remove(String clientId) {
        clientChannels.remove(clientId);
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().remoteAddress().toString();
        System.out.println("客户端 " + clientId + " 已连接");
    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(CLIENT_ID_KEY).get();
        if (clientId != null) {
            clientChannels.remove(clientId);
            System.out.println("客户端 " + clientId + " 已断开");
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("客户端 " + ctx.channel().remoteAddress().toString() + " 出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}