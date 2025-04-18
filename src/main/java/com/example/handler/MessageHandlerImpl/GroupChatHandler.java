package com.example.handler.MessageHandlerImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.controller.GroupController;
import com.example.handler.MessageHandler;
import com.example.handler.SessionManager;
import com.example.util.SpringContext;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

public class GroupChatHandler implements MessageHandler {
    @Override
    public void handle(JSONObject jsonMsg, ChannelHandlerContext ctx) {
        /*
         * 要求信息：
         * clientId: 通过token和redis获取的客户端的ID
         * token需要通过header获取
         * groupId: 群聊的ID
         */
        String clientId = jsonMsg.getString("UserId");
        String groupId = jsonMsg.getString("groupId");
        GroupController groupController = SpringContext.getBean(GroupController.class);
        List<Integer> members = groupController.getUsers(Integer.parseInt(groupId));
        for (Integer member : members) {
            SessionManager.get(member.toString()).writeAndFlush(jsonMsg);
        }
        sendResponse(ctx, HttpResponseStatus.ACCEPTED, groupId);
    }
}
