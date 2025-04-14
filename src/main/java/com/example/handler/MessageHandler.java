package com.example.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler {
    void handle(JSONObject jsonMsg, ChannelHandlerContext ctx);
}