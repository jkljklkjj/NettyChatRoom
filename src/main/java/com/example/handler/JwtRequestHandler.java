package com.example.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import com.example.util.JwtService;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Autowired
    private JwtService jwtService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            // 解析请求头
            String authorizationHeader = msg.headers().get("Authorization");
//            System.out.println("Request URL: " + msg.uri());

            int userId = jwtService.validateTokenAndExtractUser(authorizationHeader);
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