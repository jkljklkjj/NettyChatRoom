package com.example.controller;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestAttribute;

import com.example.annotation.RequireUserId;
import com.example.common.api.ApiResponse;
import com.example.dto.AddMemberRequest;
import com.example.dto.GroupIdRequest;
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
    @RequireUserId
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping("/register")
    public ApiResponse<Integer> register(@RequestAttribute(value = "UserId", required = false) Integer userId,
                                          @RequestParam Group group) {
        int groupId = groupService.register(group);
        MongoGroup mongoGroup = new MongoGroup(groupId, userId);
        mongoGroupService.register(mongoGroup);
        return ApiResponse.success(groupId);
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
     * @return 群聊列表
     */
    @ApiOperation(value = "获取用户群聊列表")
    @RequireUserId
    @GetMapping("/get")
    public List<Group> getGroup(@RequestAttribute(value = "UserId", required = false) Integer userId) {
        return mongoGroupService.getGroups(userId);
    }

    /**
     * 获取群聊成员
     * @param id 群聊ID
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
     * @return 是否成功
     */
    @ApiOperation(value = "群聊添加成员")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/addMember")
    public boolean addMember(@RequestBody AddMemberRequest request) {
        MongoGroup mongoGroup = mongoGroupService.getGroup(request.getGroupId());
        MongoUser user = mongoUserService.getUserByUserId(request.getUserId());
        if (mongoGroup == null || user == null) {
            return false;
        }
        return mongoGroupService.addMember(mongoGroup.getGroupId(), user.getUserId());
    }

    /**
     * 删除群聊
     * @return 是否成功
     */
    @ApiOperation(value = "删除群聊")
    @RequireUserId
    @PostMapping("/del")
    public boolean delGroup(@RequestAttribute(value = "UserId", required = false) Integer userId,
                             @RequestBody GroupIdRequest req) {
        int groupId = req.getGroupId();
        MongoGroup mongoGroup = mongoGroupService.getGroup(groupId);
        if (mongoGroup == null || mongoGroup.getAdmin() != userId) {
            return false;
        }
        int group = groupService.delGroup(groupId);
        if (group == 0) {
            return false;
        }
        return mongoGroupService.delGroup(groupId);
    }
}
