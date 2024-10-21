package com.example.service.mongo;

import org.springframework.stereotype.Service;
import com.example.repository.MongoGroupRepository;
import com.example.model.mongo.MongoGroup;
import org.bson.types.ObjectId;

@Service
public class MongoGroupService {
    private final MongoGroupRepository mongoGroupRepository;

    private MongoGroupService(MongoGroupRepository mongoGroupRepository) {
        this.mongoGroupRepository = mongoGroupRepository;
    }

    /**
     * 在MonggoDB中注册群聊
     * @param group 群聊对象
     * @return 是否注册成功
     */
    public boolean register(MongoGroup group) {
        if (mongoGroupRepository.findByGroupid(group.getGroupid()) != null) {
            return false;
        }
        mongoGroupRepository.save(group);
        return true;
    }

    /**
     * 添加成员
     * @param groupId 群组在Mysql的ID
     * @param memberId 成员的MongoDB的ID
     * @return 是否成功
     */
    public boolean addMember(int groupId, int memberId) {
        MongoGroup group = mongoGroupRepository.findByGroupid(groupId);
        if (group == null) {
            return false;
        }
        return group.addMember(memberId);
    }

    /**
     * 删除成员
     * @param groupId 群组在Mysql的ID
     * @param memberId 成员的MongoDB的ID
     * @return 是否成功
     */
    public boolean removeMember(int groupId, ObjectId memberId) {
        MongoGroup group = mongoGroupRepository.findByGroupid(groupId);
        if (group == null) {
            return false;
        }
        return group.removeMember(memberId);
    }

    public MongoGroup getGroup(int groupid) {
        return mongoGroupRepository.findByGroupid(groupid);
    }

    public boolean delGroup(int groupid){
        MongoGroup group = mongoGroupRepository.findByGroupid(groupid);
        if (group == null) {
            return false;
        }
        mongoGroupRepository.delete(group);
        return true;
    }
}
