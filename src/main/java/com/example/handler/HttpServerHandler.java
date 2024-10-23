package com.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        System.out.println("HTTP请求处理中");
        if (msg instanceof FullHttpRequest request) {
            System.out.println("接收到HTTP请求：" + request.uri());
            // 获取HTTP请求头部中的JSON
            String jsonHeader = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
            if (jsonHeader != null) {
                System.out.println("HTTP请求头部中的JSON：" + jsonHeader);
            }

            String jsonBody = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
            System.out.println("HTTP请求体中的JSON：" + jsonBody);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("HTTP请求处理完成");
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