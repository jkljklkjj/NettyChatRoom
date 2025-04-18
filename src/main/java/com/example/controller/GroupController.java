package com.example.controller;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.mongo.MongoGroup;
import com.example.model.mongo.MongoUser;
import com.example.model.mysql.Group;
import com.example.service.mongo.MongoGroupService;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.GroupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 群聊相关接口
 * 提供了创建群聊，获取群聊信息等接口
 */
@Api(tags = "群聊管理")
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
    @ApiOperation(value = "注册群聊")
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping("/register")
    public int register(@RequestAttribute("UserId") int id, @RequestParam Group group) {
        int groupId = groupService.register(group);
        MongoGroup mongoGroup = new MongoGroup(groupId, id);
        if(!mongoGroupService.register(mongoGroup)){
            throw new RuntimeException("MongoDB register failed");
        }
        return groupId;
    }

    /**
     * 获取群聊信息
     * @param id 群聊ID
     * @return 群聊信息
     */
    @ApiOperation(value = "获取群聊信息")
    @RequestMapping("/get/detail")
    public Group get(@RequestParam int id) {
        return groupService.selectGroup(id);
    }

    /**
     * 获取群聊列表
     * @param id 用户 ID
     * @return 群聊列表
     */
    @ApiOperation(value = "获取好友列表")
    @GetMapping("/get")
    public List<Group> getGroup(@RequestAttribute("UserId") int id) {
        return mongoGroupService.getGroups(id);
    }

    /**
     * 获取群聊成员
     * @param groupId 群聊ID
     * @return 成员ID列表
     */
    @ApiOperation(value = "获取群聊成员")
    @RequestMapping("/getUsers")
    public List<Integer> getUsers(@RequestParam int id) {
        MongoGroup mongoGroup = mongoGroupService.getGroup(id);
        return mongoGroup.getMembers();
    }

    /**
     * 群聊添加成员
     * @param groupId 群聊ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @ApiOperation(value = "群聊添加成员")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/addMember")
    public boolean addMember(int groupId, int userId) {
        MongoGroup mongoGroup = mongoGroupService.getGroup(groupId);
        MongoUser user = mongoUserService.getUserByUserId(userId);
        if(mongoGroup == null || user == null){
            return false;
        }
        return mongoGroupService.addMember(mongoGroup.getGroupId(), user.getUserId());
    }

    /**
     * 删除群聊
     * @param id 群聊ID
     * @return 是否成功
     */
    @ApiOperation(value = "删除群聊")
    @PostMapping("del")
    public boolean delGroup(@RequestAttribute("UserId") int id,@RequestParam int groupId) {
        MongoGroup mongoGroup = mongoGroupService.getGroup(groupId);
        if(mongoGroup == null || mongoGroup.getAdmin() != id){
            return false;
        }

        int group = groupService.delGroup(groupId);
        if(group == 0){
            return false;
        }
        return mongoGroupService.delGroup(groupId);
    }
}
