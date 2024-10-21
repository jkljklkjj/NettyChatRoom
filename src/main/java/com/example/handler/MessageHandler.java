package com.example.handler;

import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler {
    void handle(com.alibaba.fastjson.JSONObject jsonMsg, ChannelHandlerContext ctx);
}