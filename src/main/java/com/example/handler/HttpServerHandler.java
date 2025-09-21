package com.example.handler;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandlerImpl.MessageHandlerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            System.out.println("HTTP请求处理中");
            if (msg instanceof FullHttpRequest request) {
                System.out.println("接收到HTTP请求：" + request);
                String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
                String body = request.content().toString(CharsetUtil.UTF_8);
                if (contentType != null && contentType.contains("application/json")) {
                    JSONObject jsonMsg = JSONObject.parseObject(body);
                    System.out.println("转换后的JSON对象：" + jsonMsg);
                    String type = jsonMsg.getString("type");
                    MessageHandler handler = MessageHandlerFactory.create(type);
                    if (handler != null) {
                        handler.handle(jsonMsg, ctx);
                    } else{
                        throw new Exception("没有找到对应业务的处理器"+type);
                    }
                } else {
                    String jsonBody = request.content().toString(CharsetUtil.UTF_8);
                    System.out.println("HTTP请求体中的JSON：" + jsonBody);
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
//        System.out.println("HTTP请求处理完成");
        ctx.flush();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("HTTP请求处理器已添加");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("HTTP请求处理器已移除");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}