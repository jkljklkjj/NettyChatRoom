package com.example.service.mongo;

import java.util.ArrayList;
import java.util.List;

import com.example.mapper.UserMapper;
import com.example.model.mysql.Group;
import com.example.model.mysql.User;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.mongo.MongoGroup;
import com.example.model.mongo.MongoUser;
import com.example.repository.MongoGroupRepository;
import com.example.repository.MongoUserRepository;
import com.example.service.mysql.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MongoUserService {
    private static final Logger log = LoggerFactory.getLogger(MongoUserService.class);
    private final MongoUserRepository mongoUserRepository;
    private final MongoGroupRepository mongoGroupRepository;
    private final UserMapper userMapper;
    private final GroupService groupService;

    public MongoUserService(MongoUserRepository mongoUserRepository, MongoGroupRepository mongoGroupRepository, UserMapper userMapper, GroupService groupService) {
        this.mongoUserRepository = mongoUserRepository;
        this.mongoGroupRepository = mongoGroupRepository;
        this.userMapper = userMapper;
        this.groupService = groupService;
    }

    /**
     * 注册用户
     * @param user MongoUser对象
     * @return 是否注册成功
     */
    public boolean register(MongoUser user) {
        log.info("Mongo 注册用户 userId={}", user.getUserId());
        if (mongoUserRepository.findByUserId(user.getUserId()) != null) {
            log.warn("用户已存在 userId={}", user.getUserId());
            return false;
        }
        mongoUserRepository.save(user);
        return true;
    }

    /**
     * 查询用户对象
     * @param id MongoDB中的主键
     * @return 用户对象
     */
    public MongoUser getUserById(ObjectId id) {
        return mongoUserRepository.findById(id).orElse(null);
    }

    /**
     * 查询用户对象
     * @param userId MySQL中的用户ID
     * @return Mongo用户对象
     */
    public MongoUser getUserByUserId(int userId) {
        return mongoUserRepository.findByUserId(userId);
    }

    /**
     * 添加好友
     * @param userId 当前用户Mysql的ID
     * @param friendId 好友的Mysql的ID
     * @return 是否成功
     */
    @Transactional
    public boolean addFriend(int userId, int friendId) {
        if(userId == friendId){
            log.warn("不能添加自己为好友 userId={}", userId);
            return false;
        }
        MongoUser user = mongoUserRepository.findByUserId(userId);
        MongoUser friend = mongoUserRepository.findByUserId(friendId);
        if (user == null || friend == null) {
            log.warn("添加好友失败：用户不存在 src={} target={}", user, friend);
            return false;
        }
        if (user.addFriend(friendId) && friend.addFriend(userId)) {
            mongoUserRepository.save(user);
            mongoUserRepository.save(friend);
            log.info("添加好友成功 userId={} friendId={}", userId, friendId);
            return true;
        }
        return false;
    }

    /**
     * 删除好友
     * @param userId 当前用户Mysql的ID
     * @param friendId 好友的Mysql的ID
     * @return 是否成功
     */
    @Transactional
    public boolean delFriend(int userId,int friendId){
        MongoUser user = mongoUserRepository.findByUserId(userId);
        MongoUser friend = mongoUserRepository.findByUserId(friendId);
        if (user == null || friend == null) {
            return false;
        }
        if (user.delFriend(friendId) && friend.delFriend(userId)) {
            mongoUserRepository.save(user);
            mongoUserRepository.save(friend);
            log.info("删除好友成功 userId={} friendId={}", userId, friendId);
            return true;
        }
        return false;
    }

    /**
     * 获取好友列表
     * @param userId 当前用户Mysql的ID
     * @return 是否成功
     */
    public List<User> getFriends(int userId){
        MongoUser mongoUser = mongoUserRepository.findByUserId(userId);
        if(mongoUser == null){
            log.warn("用户不存在 userId={}", userId);
            return new ArrayList<>();
        }
        List<Integer> friends = mongoUser.getFriends();
        if(friends.isEmpty()){
            return new ArrayList<>();
        }
        return userMapper.selectFriends(friends);
    }

    /**
     * 获取用户的群组列表
     * @param userId 当前用户Mysql的ID
     * @return 群组列表
     */
    public List<Group> getGroups(int userId) {
        MongoUser user = mongoUserRepository.findByUserId(userId);
        if(user == null){
            log.warn("用户不存在 userId={}", userId);
            return new ArrayList<>();
        }
        List<Integer> groups = user.getGroups();
        if(groups.isEmpty()){
            return new ArrayList<>();
        }
        return groupService.selectGroups(groups);
    }

    /**
     * 添加群组
     * @param userId 当前用户Mysql的ID
     * @param groupId 群组的Mysql的ID
     * @return 是否成功
     */
    @Transactional
    public boolean addGroup(int userId, int groupId) {
        MongoUser user = mongoUserRepository.findByUserId(userId);
        MongoGroup group = mongoGroupRepository.findByGroupId(groupId);
        if (user == null || group == null) {
            log.warn("添加群组失败 userId={} groupId={} user/group缺失", userId, groupId);
            return false;
        }
        if (user.addGroup(groupId) && group.addMember(userId)) {
            mongoUserRepository.save(user);
            mongoGroupRepository.save(group);
            log.info("添加群组成功 userId={} groupId={}", userId, groupId);
            return true;
        }
        return false;
    }
}