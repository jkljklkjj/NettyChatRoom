package com.example.handler.MessageHandlerImpl;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.LoggerFactory;

public class ChatHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);
    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        /*
         * 要求信息：
         * targetClientId: 目标客户端的ID
         * content: 聊天内容
         */
        String targetClientId = jsonMsg.getString("targetClientId");
        String content = jsonMsg.getString("content");
        String UserId = jsonMsg.getString("UserId");
        // TODO 先通过redis的位图检测在线状态
        // TODO 如果不在线且用户存在，则把信息放到消息队列
        ChannelHandlerContext targetChannel = SessionManager.get(targetClientId);
        if (targetChannel != null) {
            System.out.println("发送给客户端 " + targetClientId + " 的消息：" + content+" from "+UserId);
            ChannelFuture future = targetChannel.writeAndFlush(Unpooled.copiedBuffer(content.getBytes()));
            future.addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("消息发送成功");
                } else {
                    System.out.println("消息发送失败");
                    f.cause().printStackTrace();
                    logger.info("消息发送失败");
                }
            });
        } else {
            System.out.println("客户" + ctx.channel().remoteAddress().toString() + "试图发送信息的时候出错");
            ctx.writeAndFlush(Unpooled.copiedBuffer("该好友不在线".getBytes()));
        }
    }
    
}
