package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;
import com.example.service.redis.RedisService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "好友管理")
@RestController
@RequestMapping("/friend")
public class FriendController {
    private final UserService userService;
    private final MongoUserService mongoUserService;


    public FriendController(UserService userService, MongoUserService mongoUserService) {
        this.userService = userService;
        this.mongoUserService = mongoUserService;
    }

    /**
     * 添加好友
     * @param id 用户 ID
     * @param friendId 好友 ID
     * @return 是否添加成功
     */
    @ApiOperation(value = "添加好友")
    @PostMapping("/add")
    public boolean addFriend(@RequestAttribute("UserId") int id, @RequestParam(name = "friendId", required = true) int friendId) {
        return mongoUserService.addFriend(id, friendId);
    }

    /**
     * 删除好友
     * @param id 用户 ID
     * @param friendId 好友 ID
     * @return 是否删除成功
     */
    @ApiOperation(value = "删除好友")
    @PostMapping("/del")
    public boolean delFriend(@RequestAttribute("UserId") int id, @RequestParam(name = "friendId", required = true) int friendId) {
        return mongoUserService.delFriend(id, friendId);
    }

    /**
     * 获取好友列表
     * @param id 用户 ID
     * @return 好友列表
     */
    @ApiOperation(value = "获取好友列表")
    @PostMapping("/get")
    public List<User> getFriend(@RequestAttribute("UserId") int id) {
        return mongoUserService.getFriends(id);
    }

}
