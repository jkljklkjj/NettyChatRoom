package com.example.handler.MessageHandlerImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class CheckHandler implements MessageHandler {
        @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        /*
         * 要求信息：
         * clientId: 通过token和redis获取的客户端的ID
         * token需要通过header获取
         */
        String clientId = jsonMsg.getString("targetClientId");
        boolean online = SessionManager.isOnline(clientId);
    
        // 构造响应消息
        JSONObject response = new JSONObject();
        response.put("clientId", clientId);
        response.put("online", online);
    
        // 将响应消息转换为字节数组
        byte[] responseBytes = response.toJSONString().getBytes(CharsetUtil.UTF_8);
    
        // 返回响应
        DefaultFullHttpResponse httpResponse;
        if (online) {
            // 用户在线，返回 200 状态码
            httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(responseBytes)
            );
        } else {
            // 用户不在线，返回 404 状态码
            httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_FOUND,
                    Unpooled.wrappedBuffer(responseBytes)
            );
        }
    
        // 设置 Content-Length 和 Content-Type
        httpResponse.headers().set("Content-Length", responseBytes.length);
        httpResponse.headers().set("Content-Type", "application/json; charset=UTF-8");
    
        // 写回响应
        ctx.writeAndFlush(httpResponse);
    }

}
