package com.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class TimeoutHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        AttributeKey<String> CLIENT_ID_KEY = SessionManager.CLIENT_ID_KEY;

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("读超时，关闭连接");
                ctx.close();
                SessionManager.remove(ctx.channel().attr(CLIENT_ID_KEY).get());
            } else if (event.state() == IdleState.WRITER_IDLE) {
                System.out.println("写超时");
                // 可选择发送心跳包或其他逻辑
            } else if (event.state() == IdleState.ALL_IDLE) {
                System.out.println("读写超时，关闭连接");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}