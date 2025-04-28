package com.example.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.constant.ChannelConstant;
import com.example.constant.RedisPrefixConstant;
import com.example.model.mysql.Group;
import com.example.service.mongo.MongoGroupService;
import io.netty.channel.Channel;
import com.example.service.redis.RedisService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    // key: 用户Id, value: channelId
    private static final Map<String, Channel> clientChannelMap = new ConcurrentHashMap<>();
    // key: 群组id, value: channelGroup
    private static final Map<String, ChannelGroup> groupChannelMap = new ConcurrentHashMap<>();
    // 新增channel的用户账号和所在群聊信息
    public static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf(ChannelConstant.CLIENT_ID_KEY);
    public static final AttributeKey<List<Integer>> GROUP_ID_KEY = AttributeKey.valueOf("groupId");

    private static RedisService redisService;
    private static MongoGroupService mongoGroupService;

    @Autowired
    public void setRedisService(RedisService redisService, MongoGroupService mongoGroupService) {
        SessionManager.redisService = redisService;
        SessionManager.mongoGroupService = mongoGroupService;
    }

    public SessionManager() {
    }

    public static Channel get(String clientId) {
        Channel channel = clientChannelMap.get(clientId);
        if (channel != null) {
            return channel;
        }
        logger.info("用户 {} 不在线", clientId);
        return null;
    }

    /**
     * 用户上线
     */
    public static void add(String clientId, ChannelHandlerContext ctx) {
        // 设置用户在线状态
        redisService.setBit(RedisPrefixConstant.USER_ONLINE_STATUS_KEY, Integer.valueOf(clientId), true);
        clientChannelMap.put(clientId, ctx.channel());
        List<Group> groups = mongoGroupService.getGroups(Integer.valueOf(clientId));
        List<Integer> groupIds = groups.stream().map(Group::getId).toList();
        ctx.channel().attr(GROUP_ID_KEY).set(groupIds);
        ctx.channel().attr(CLIENT_ID_KEY).set(clientId);
        groupIds.forEach(groupId -> {addToGroup(groupId.toString(), ctx.channel());});
    }

    /**
     * 添加用户到群组
     */
    public static void addToGroup(String groupId, Channel channel) {
        groupChannelMap.computeIfAbsent(groupId, k -> new DefaultChannelGroup(channel.eventLoop())).add(channel);
        logger.info("用户 {} 加入群组 {}", channel.id(), groupId);
    }

    /**
     * 获取群组
     */
    public static ChannelGroup getGroup(String groupId) {
        return groupChannelMap.get(groupId);
    }

    /**
     * 用户下线
     */
    public static void remove(String clientId) {
        // 设置用户离线状态
        redisService.setBit(RedisPrefixConstant.USER_ONLINE_STATUS_KEY, Integer.valueOf(clientId), false);
        Channel channel = clientChannelMap.remove(clientId);
        List<Integer> groupIds = channel.attr(GROUP_ID_KEY).get();
        groupIds.forEach(groupId -> {removeFromGroup(groupId.toString(), channel);});
        if (channel != null) {
            channel.close();
            logger.info("用户 {} 下线", clientId);
        } else {
            logger.warn("用户 {} 不在线", clientId);
        }
    }

    /**
     * 从群组移除用户
     */
    public static void removeFromGroup(String groupId, Channel channel) {
        ChannelGroup group = groupChannelMap.get(groupId);
        if (group != null) {
            group.remove(channel);
            logger.info("用户 {} 从群组 {} 移除", channel.id(), groupId);
            if (group.isEmpty()) {
                groupChannelMap.remove(groupId);
                logger.info("群组 {} 已清空", groupId);
            }
        }
    }

    public static boolean isOnline(String clientId) {
        if(!redisService.getBit(RedisPrefixConstant.USER_ONLINE_STATUS_KEY,Integer.valueOf(clientId))){
            return false;
        }
        return clientChannelMap.containsKey(clientId);
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().remoteAddress().toString();
        System.out.println("客户端 " + clientId + " 已连接");
    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(CLIENT_ID_KEY).get();
        if (clientId != null) {
            remove(clientId);
            System.out.println("客户端 " + clientId + " 已断开");
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("客户端 " + ctx.channel().remoteAddress().toString() + " 出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}