package com.example.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.constant.ChannelConstant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    // key: 用户Id, value: channelId
    private static final Map<String, ChannelId> clientChannelMap = new ConcurrentHashMap<>();
    // 新增channel的信息
    private static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf(ChannelConstant.CLIENT_ID_KEY);
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public SessionManager() {
    }

    public static ChannelHandlerContext get(String clientId) {
        ChannelId channelId = clientChannelMap.get(clientId);
        if (channelId != null) {
            Channel channel = CHANNEL_GROUP.find(channelId);
            if (channel != null) {
                return channel.pipeline().context(clientId+"chatting");
            }
        }
        logger.info("用户 {} 不在线", clientId);
        return null;
    }

    /**
     * 用户上线
     */
    public static void add(String clientId, ChannelHandlerContext ctx) {
        ChannelId channelId = ctx.channel().id();
        ctx.channel().attr(CLIENT_ID_KEY).set(clientId);
        clientChannelMap.put(clientId, channelId);
    }

    /**
     * 用户下线
     */
    public static void remove(String clientId) {
        ChannelId channelId = clientChannelMap.remove(clientId);
        if (channelId != null) {
            Channel channel = CHANNEL_GROUP.find(channelId);
            if (channel != null) {
                channel.close();
            }
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