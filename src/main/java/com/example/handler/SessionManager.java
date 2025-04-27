package com.example.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.constant.ChannelConstant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    // key: 用户Id, value: channelId
    private static final Map<String, Channel> clientChannelMap = new ConcurrentHashMap<>();
    // 新增channel的信息
    private static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf(ChannelConstant.CLIENT_ID_KEY);

    public SessionManager() {
    }

    public static Channel get(String clientId) {
        Channel channel = clientChannelMap.get(clientId);
        if (channel != null) {
            return channel;
        }
        logger.info("用户 {} 不在线", clientId);
        return null;
    }

    /**
     * 用户上线
     */
    public static void add(String clientId, ChannelHandlerContext ctx) {
        ctx.channel().attr(CLIENT_ID_KEY).set(clientId);
        clientChannelMap.put(clientId, ctx.channel());
    }

    /**
     * 用户下线
     */
    public static void remove(String clientId) {
        Channel channel = clientChannelMap.remove(clientId);
        if (channel != null) {
            channel.close();
            logger.info("用户 {} 下线", clientId);
        } else {
            logger.warn("用户 {} 不在线", clientId);
        }
    }

    public static boolean isOnline(String clientId) {
        return clientChannelMap.containsKey(clientId);
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().remoteAddress().toString();
        System.out.println("客户端 " + clientId + " 已连接");
    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(CLIENT_ID_KEY).get();
        if (clientId != null) {
            remove(clientId);
            System.out.println("客户端 " + clientId + " 已断开");
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("客户端 " + ctx.channel().remoteAddress().toString() + " 出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}