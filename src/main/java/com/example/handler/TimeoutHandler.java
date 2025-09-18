package com.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(TimeoutHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        AttributeKey<String> CLIENT_ID_KEY = SessionManager.CLIENT_ID_KEY;

        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                log.warn("读超时，关闭连接 clientId={}", ctx.channel().attr(CLIENT_ID_KEY).get());
                ctx.close();
                SessionManager.remove(ctx.channel().attr(CLIENT_ID_KEY).get());
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.debug("写空闲 clientId={}", ctx.channel().attr(CLIENT_ID_KEY).get());
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.warn("读写超时，关闭连接 clientId={}", ctx.channel().attr(CLIENT_ID_KEY).get());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}