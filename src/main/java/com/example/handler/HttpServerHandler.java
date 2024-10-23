package com.example.handler;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandlerImpl.MessageHandlerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MixedFileUpload;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            System.out.println("HTTP请求处理中");
            if (msg instanceof FullHttpRequest request) {
                System.out.println("接收到HTTP请求：" + request);
                String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
                System.out.println("HTTP请求头中的Content-Type：" + contentType);
                String body = request.content().toString(CharsetUtil.UTF_8);
                System.out.println("HTTP请求体：" + body);
                if (contentType != null && contentType.contains("application/json")) {
                    JSONObject jsonMsg = JSONObject.parseObject(body);
                    System.out.println("转换后的JSON对象：" + jsonMsg);
                    // TODO：将jwtToken获取的用户id放入jsonMsg中
                    String type = jsonMsg.getString("type");
                    MessageHandler handler = MessageHandlerFactory.create(type);
                    if (handler != null) {
                        handler.handle(jsonMsg, ctx);
                    }
                } else if (contentType.startsWith("multipart/form-data")) {
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                    decoder.offer(request);

                    while (decoder.hasNext()) {
                        InterfaceHttpData data = decoder.next();
                        if (data != null) {
                            try {
                                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                                    Attribute attribute = (Attribute) data;
                                    System.out.println("Attribute: " + attribute.getName() + " = " + attribute.getValue());
                                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                                    MixedFileUpload fileUpload = (MixedFileUpload) data;
                                    System.out.println("File: " + fileUpload.getFilename() + " = " + fileUpload.getString(CharsetUtil.UTF_8));
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                data.release();
                            }
                        }
                    }
                } else {
                    String jsonBody = request.content().toString(CharsetUtil.UTF_8);
                    System.out.println("HTTP请求体中的JSON：" + jsonBody);
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        } finally {
            // No need to release the buffer here
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