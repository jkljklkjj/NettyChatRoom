package com.example.service.mongo;

import java.util.List;

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

    public MongoUserService(MongoUserRepository mongoUserRepository, MongoGroupRepository mongoGroupRepository) {
        this.mongoUserRepository = mongoUserRepository;
        this.mongoGroupRepository = mongoGroupRepository;
    }

    /**
     * 注册用户
     * @param user MongoUser对象
     * @return 是否注册成功
     */
    public boolean register(MongoUser user) {
        System.out.println("正在MongoDB中注册用户：");
        if (mongoUserRepository.findByUserId(user.getUserId()) != null) {
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
    public List<Integer> getFriends(int userId){
        MongoUser user = mongoUserRepository.findByUserId(userId);
        return user.getFriends();
    }

    /**
     * 添加群组
     * @param userId 当前用户Mysql的ID
     * @param groupId 群组的Mysqll的ID
     * @return 是否成功
     */
    @Transactional
    public boolean addGroup(int userId, int groupId) {
        MongoUser user = mongoUserRepository.findByUserId(userId);
        MongoGroup group = mongoGroupRepository.findByGroupid(groupId);
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