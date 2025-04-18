package com.example.handler;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

import com.example.util.JwtUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

@Component
public class JwtRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            // 解析请求头
            String authorizationHeader = msg.headers().get("Authorization");
            int userId = JwtUtil.validateTokenAndExtractUser(authorizationHeader);
            if (userId != 0) {
                // 将用户ID添加到请求body中
                String body = msg.content().toString(StandardCharsets.UTF_8);
                body += "\nUserId: " + userId;
                ByteBuf content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
                msg.content().clear().writeBytes(content);
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