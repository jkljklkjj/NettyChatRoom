package com.example.handler.MessageHandlerImpl;

import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.buffer.Unpooled;
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
import org.springframework.stereotype.Component;

@Component
public class GroupChatHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);

    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        String targetClientId = jsonMsg.getString("targetClientId");
        String content = jsonMsg.getString("content");
        String userId = jsonMsg.getString("UserId");
        String timestamp = jsonMsg.getString("timestamp");

        // 构建请求体
        FullHttpRequest httpRequest = buildHttpRequest(content, timestamp);

        ChannelGroup channelGroup = SessionManager.getGroup(targetClientId);
        System.out.println("发送给群聊 " + targetClientId + " 的消息：" + content + " from " + userId);

        // 发送消息
        sendMessage(ctx, channelGroup, content);
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
     *
     * @param ctx     信息发送客户
     * @param channels 信息接收群聊
     * @param message 信息内容
     */
    private void sendMessage(ChannelHandlerContext ctx, ChannelGroup channels, String message) {
        byte[] responseBytes = message.getBytes(CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseBytes));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseBytes.length);

        if (channels != null) {
            // TODO 处理群聊中不在线成员
            // 排除发送者的 Channel
            channels.writeAndFlush(response, channel -> channel != ctx.channel()).addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("群聊消息发送成功");
                    sendResponse(ctx, "群聊消息发送成功", HttpResponseStatus.OK);
                } else {
                    logger.info("群聊消息发送失败");
                    f.cause().printStackTrace();
                    sendResponse(ctx, "群聊消息发送失败", HttpResponseStatus.ACCEPTED);
                }
            });
        } else {
            sendResponse(ctx, "群组不存在", HttpResponseStatus.NOT_FOUND);
        }
    }
}