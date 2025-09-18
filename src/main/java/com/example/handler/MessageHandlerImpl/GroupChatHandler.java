package com.example.handler.MessageHandlerImpl;

import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;

@Component
public class GroupChatHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(GroupChatHandler.class);

    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        String targetClientId = jsonMsg.getString("targetClientId");
        String content = jsonMsg.getString("content");
        String userId = jsonMsg.getString("UserId");
        if (targetClientId == null) {
            log.warn("群聊发送失败: targetClientId 为空 userId={}", userId);
            sendResponse(ctx, "群组ID为空", HttpResponseStatus.BAD_REQUEST);
            return;
        }
        ChannelGroup channelGroup = SessionManager.getGroup(targetClientId);
        log.info("群聊消息 -> groupId={} from={} content={}", targetClientId, userId, content);
        sendMessage(ctx, channelGroup, content);
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
            channels.writeAndFlush(response, ch -> ch != ctx.channel()).addListener(f -> {
                if (f.isSuccess()) {
                    log.debug("群聊消息发送成功 size={}", channels.size());
                    sendResponse(ctx, "群聊消息发送成功", HttpResponseStatus.OK);
                } else {
                    log.error("群聊消息发送失败", f.cause());
                    sendResponse(ctx, "群聊消息发送失败", HttpResponseStatus.ACCEPTED);
                }
            });
        } else {
            log.warn("群聊不存在，无法发送");
            sendResponse(ctx, "群组不存在", HttpResponseStatus.NOT_FOUND);
        }
    }
}