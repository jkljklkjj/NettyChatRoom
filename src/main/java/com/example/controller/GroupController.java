package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.mongo.MongoGroup;
import com.example.model.mongo.MongoUser;
import com.example.model.mysql.Group;
import com.example.service.mongo.MongoGroupService;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.GroupService;

/**
 * 群聊相关接口
 * 提供了创建群聊，获取群聊信息等接口
 */
@RestController
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;
    private final MongoGroupService mongoGroupService;
    private final MongoUserService mongoUserService;

    public GroupController(GroupService groupService, MongoGroupService mongoGroupService, MongoUserService mongoUserService) {
        this.groupService = groupService;
        this.mongoGroupService = mongoGroupService;
        this.mongoUserService = mongoUserService;
    }

    /**
     * 注册群聊
     * @param group Mysql中的群聊映射
     * @return 是否成功
     */
    @GetMapping("/register")
    public boolean register(@RequestParam Group group) {
        int id = groupService.register(group);
        MongoGroup mongoGroup = new MongoGroup(id);
        return mongoGroupService.register(mongoGroup);
    }

    /**
     * 获取群聊信息
     * @param id 群聊ID
     * @return 群聊信息
     */
    @GetMapping("/get")
    public Group get(@RequestParam int id) {
        return groupService.selectGroup(id);
    }

    /**
     * 获取群聊成员
     * @param id 群聊ID
     * @return 成员ID列表
     */
    @GetMapping("/getUsers")
    public List<Integer> getUsers(@RequestParam int id) {
        MongoGroup mongoGroup = mongoGroupService.getGroup(id);
        List<Integer> members = mongoGroup.getMembers();
        List<Integer> result = new ArrayList<>();
        MongoUser user;
        for(int member : members){
            user = mongoUserService.getUserByUserId(member);
            result.add(user.getUserId());
        }
        return result;
    }

    /**
     * 群聊添加成员
     * @param groupId 群聊ID
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean addMember(int groupId, int userId) {
        MongoGroup mongoGroup = mongoGroupService.getGroup(groupId);
        MongoUser user = mongoUserService.getUserByUserId(userId);
        if(mongoGroup == null || user == null){
            return false;
        }
        return mongoGroupService.addMember(mongoGroup.getGroupid(), user.getUserId());
    }

    /**
     * 删除群聊
     * @param id 群聊ID
     * @return 是否成功
     */
    @GetMapping("del")
    public boolean delGroup(@RequestParam int id) {
        Group group = groupService.delGroup(id);
        if(group == null){
            return false;
        }
        return mongoGroupService.delGroup(id);
    }
}
