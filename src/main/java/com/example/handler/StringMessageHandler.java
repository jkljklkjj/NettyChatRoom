package com.example.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandlerImpl.MessageHandlerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class StringMessageHandler extends SimpleChannelInboundHandler<String> {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StringMessageHandler.class);
    // key: 用户Id value: 通道上下文
    private static final Map<String, ChannelHandlerContext> clientChannels = new ConcurrentHashMap<>();
    private static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf("clientId");

    public StringMessageHandler() {
    }

    public static ChannelHandlerContext get(String clientId) {
        return clientChannels.get(clientId);
    }

    public static void add(String clientId, ChannelHandlerContext ctx) {
        clientChannels.put(clientId, ctx);
    }

    public static void remove(String clientId) {
        clientChannels.remove(clientId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        try {
            System.out.println("接收到的字符串消息：" + msg);
            JSONObject jsonMsg = JSONObject.parseObject(msg);
            String type = jsonMsg.getString("type");
            MessageHandler handler = MessageHandlerFactory.create(type);
            handler.handle(jsonMsg, ctx);
        } catch (Exception e) {
            System.out.println("接收到的消息不是有效的 JSON 格式");
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().remoteAddress().toString();
        System.out.println("客户端 " + clientId + " 已连接");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(CLIENT_ID_KEY).get();
        if (clientId != null) {
            clientChannels.remove(clientId);
            System.out.println("客户端 " + clientId + " 已断开");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("客户端 " + ctx.channel().remoteAddress().toString() + " 出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}