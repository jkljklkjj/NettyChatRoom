package com.example.handler.MessageHandlerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class ChatHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);

    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        String targetClientId = jsonMsg.getString("targetClientId");
        String content = jsonMsg.getString("content");
        String userId = jsonMsg.getString("UserId");
        String timestamp = jsonMsg.getString("timestamp");

        // 构建请求体
        FullHttpRequest httpRequest = buildHttpRequest(content, timestamp);

        if (SessionManager.isOnline(targetClientId)) {
            ChannelHandlerContext targetChannel = SessionManager.get(targetClientId);
            System.out.println("发送给客户端 " + targetClientId + " 的消息：" + content + " from " + userId);

            // 发送消息
            sendMessage(targetChannel, content, ctx);
        } else {
            System.out.println("目标客户端不在线");
            sendResponse(ctx, "该好友不在线", HttpResponseStatus.ACCEPTED);
        }
    }

    /**
     * 构建 FullHttpRequest
     */
    private FullHttpRequest buildHttpRequest(String content, String timestamp) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("content", content);
        requestBody.put("timestamp", timestamp);

        byte[] requestBodyBytes = requestBody.toJSONString().getBytes(CharsetUtil.UTF_8);

        FullHttpRequest httpRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/chat",
                Unpooled.wrappedBuffer(requestBodyBytes));

        httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, requestBodyBytes.length);

        return httpRequest;
    }

    /**
     * 发送消息到目标客户端
     */
    private void sendMessage(ChannelHandlerContext targetChannel, String content, ChannelHandlerContext ctx) {
        // 构建 FullHttpRequest
        FullHttpRequest httpRequest = buildHttpRequest(content, String.valueOf(System.currentTimeMillis()));
        if (targetChannel.channel() == null || !targetChannel.channel().isActive()) {
            System.out.println("目标客户端的通道已失效");
            sendResponse(ctx, "目标客户端的通道已失效", HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        // 发送 HTTP 请求
        ChannelFuture future = targetChannel.channel().writeAndFlush(httpRequest);
        future.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("消息发送成功");
                sendResponse(ctx, "消息发送成功", HttpResponseStatus.OK);
            } else {
                f.cause().printStackTrace();
                logger.info("消息发送失败");
                sendResponse(ctx, "消息发送失败", HttpResponseStatus.ACCEPTED);
            }
        });
    }

    /**
     * 构建并发送响应
     */
    private void sendResponse(ChannelHandlerContext ctx, String message, HttpResponseStatus status) {
        byte[] responseBytes = message.getBytes(CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(responseBytes));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseBytes.length);

        ctx.writeAndFlush(response);
    }
}