package com.example.handler;

import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSONObject;
import com.example.service.security.JwtService; // 新增

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import static com.example.handler.SessionManager.CLIENT_ID_KEY;

/**
 * 验证请求合法性handler
 */
public class JwtRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final JwtService jwtService;

    public JwtRequestHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            // 解析请求头
            String authorizationHeader = msg.headers().get("Authorization");
            int userId = jwtService.extractUserIdFromAuthorization(authorizationHeader);
            String channelUserId = ctx.attr(CLIENT_ID_KEY).get();
            if (userId != 0 && channelUserId != null && userId == Integer.parseInt(channelUserId)) {
                // 将请求体解析为 JSON 对象
                String body = msg.content().toString(StandardCharsets.UTF_8);
                JSONObject jsonBody = JSONObject.parseObject(body);

                // 添加 UserId 字段
                jsonBody.put("UserId", userId);

                // 将 JSON 对象重新序列化为字符串
                String updatedBody = jsonBody.toJSONString();
                ByteBuf content = Unpooled.copiedBuffer(updatedBody, StandardCharsets.UTF_8);

                // 替换请求体内容
                msg.content().clear().writeBytes(content);
                msg.headers().set("Content-Length", content.readableBytes()); // 更新 Content-Length
            }
            // 如果过滤器通过，继续处理请求
            ctx.fireChannelRead(msg.retain());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}