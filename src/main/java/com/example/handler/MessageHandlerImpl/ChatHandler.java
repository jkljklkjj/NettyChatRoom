package com.example.handler.MessageHandlerImpl;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.StringMessageHandler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class ChatHandler implements MessageHandler {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ChatHandler.class);
    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        String targetClientId = jsonMsg.getString("targetClientId");
        String content = jsonMsg.getString("content");
        ChannelHandlerContext targetChannel = StringMessageHandler.get(targetClientId);
        if (targetChannel != null) {
            System.out.println("发送给客户端 " + targetClientId + " 的消息：" + content);
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