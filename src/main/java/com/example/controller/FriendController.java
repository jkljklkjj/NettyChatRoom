package com.example.controller;

import java.util.List;

import com.example.annotation.RequireUserId;
import com.example.common.api.ApiResponse;
import com.example.common.api.ErrorCode;
import com.example.dto.FriendAddRequest;
import com.example.dto.FriendIdRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestAttribute;

import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;

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
     * @param friendAddRequest 好友账号载荷
     * @return 是否添加成功
     */
    @ApiOperation(value = "添加好友")
    @RequireUserId
    @PostMapping("/add")
    public ApiResponse<Boolean> addFriend(@RequestAttribute(value = "UserId", required = false) Integer userId,
                                           @RequestBody FriendAddRequest friendAddRequest) {
        boolean result = mongoUserService.addFriend(userId, friendAddRequest.getFriendId());
        return ApiResponse.success(result);
    }

    /**
     * 删除好友
     * @return 是否删除成功
     */
    @ApiOperation(value = "删除好友")
    @RequireUserId
    @PostMapping("/del")
    public ApiResponse<Boolean> delFriend(@RequestAttribute(value = "UserId", required = false) Integer userId,
                                           @RequestBody FriendIdRequest body) {
        boolean result = mongoUserService.delFriend(userId, body.getFriendId());
        return ApiResponse.success(result);
    }

    /**
     * 获取好友列表
     * @return 好友列表
     */
    @ApiOperation(value = "获取好友列表")
    @RequireUserId
    @PostMapping("/get")
    public ApiResponse<List<User>> getFriend(@RequestAttribute(value = "UserId", required = false) Integer userId) {
        List<User> friends = mongoUserService.getFriends(userId);
        return ApiResponse.success(friends);
    }

}
