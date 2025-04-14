package com.example.service.mongo;

import java.util.ArrayList;
import java.util.List;

import com.example.mapper.UserMapper;
import com.example.model.mysql.User;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.mongo.MongoGroup;
import com.example.model.mongo.MongoUser;
import com.example.repository.MongoGroupRepository;
import com.example.repository.MongoUserRepository;

@Service
public class MongoUserService {
    private final MongoUserRepository mongoUserRepository;
    private final MongoGroupRepository mongoGroupRepository;
    private final UserMapper userMapper;

    public MongoUserService(MongoUserRepository mongoUserRepository, MongoGroupRepository mongoGroupRepository, UserMapper userMapper) {
        this.mongoUserRepository = mongoUserRepository;
        this.mongoGroupRepository = mongoGroupRepository;
        this.userMapper = userMapper;
    }

    /**
     * 注册用户
     * @param user MongoUser对象
     * @return 是否注册成功
     */
    public boolean register(MongoUser user) {
        System.out.println("正在MongoDB中注册用户：");
        if (mongoUserRepository.findByUserId(user.getUserId()) != null) {
            System.out.println("用户已存在");
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
        MongoUser user = mongoUserRepository.findByUserId(userId);
        MongoUser friend = mongoUserRepository.findByUserId(friendId);
        if (user == null || friend == null) {
            // 获取当前用户
            return false;
        }
        if (user.addFriend(friendId) && friend.addFriend(userId)) {
            mongoUserRepository.save(user); // 保存更改到数据库
            mongoUserRepository.save(friend); // 保存对方更改到数据库
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
            // 获取当前用户
            return false;
        }
        if (user.delFriend(friendId) && friend.delFriend(userId)) {
            mongoUserRepository.save(user); // 保存更改到数据库
            mongoUserRepository.save(friend); // 保存对方更改到数据库
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
        MongoUser user = mongoUserRepository.findByUserId(userId);
        List<Integer> friends = user.getFriends();
        if(friends.isEmpty()){
            return new ArrayList<>();
        }
        return userMapper.selectFriends(friends);
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
        if (user == null) {
            // 获取当前用户
            return false;
        }
        if (user.addGroup(groupId) && group.addMember(userId)) {
            mongoUserRepository.save(user); // 保存更改到数据库
            return true;
        }
        return false;
    }
}