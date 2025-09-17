package com.example.handler.MessageHandlerImpl;

import com.example.config.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        String targetClientId = jsonMsg.getString("targetClientId");
        String content = jsonMsg.getString("content");
        String userId = jsonMsg.getString("UserId");
        String timestamp = jsonMsg.getString("timestamp");

        // 构建请求体
        FullHttpRequest httpRequest = buildHttpRequest(content, timestamp);

        if (SessionManager.isOnline(targetClientId)) {
            Channel targetChannel = SessionManager.get(targetClientId);
            System.out.println("发送给客户端 " + targetClientId + " 的消息：" + content + " from " + userId);

            // 发送消息
            sendMessage(ctx,targetChannel, content);
        } else {
            // TODO 完成离线消息发送的测试
            System.out.println("目标客户端不在线");
            kafkaProducer.sendMessage(targetClientId, content);
            sendResponse(ctx, "该好友不在线", HttpResponseStatus.OK);
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
                Unpooled.copiedBuffer(requestBodyBytes));

        httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, requestBodyBytes.length);

        return httpRequest;
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

    /**
     * 转发信息
     * @param ctx 信息发送客户
     * @param channel 信息接收客户
     * @param message 信息内容
     */
    private void sendMessage(ChannelHandlerContext ctx,Channel channel, String message) {
        byte[] responseBytes = message.getBytes(CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseBytes));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseBytes.length);

        ChannelFuture future =channel.writeAndFlush(response);
        future.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("消息发送成功");
                sendResponse(ctx, "消息发送成功", HttpResponseStatus.OK);
            } else {
                logger.info("消息发送失败");
                f.cause().printStackTrace();
                sendResponse(ctx, "消息发送失败", HttpResponseStatus.ACCEPTED);
            }
        });
    }
}