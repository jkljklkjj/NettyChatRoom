package com.example.controller;

import java.util.List;

import com.example.dto.FriendAddRequest;
import com.example.dto.FriendIdRequest;
import org.springframework.web.bind.annotation.*;

import com.example.constant.SessionConstant;
import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpSession;

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
    @PostMapping("/add")
    public boolean addFriend(HttpSession session, @RequestBody FriendAddRequest friendAddRequest) {
        Integer id = (Integer) session.getAttribute(SessionConstant.USER_ID);
        if(id == null) return false;
        int friendId = friendAddRequest.getFriendId();
        return mongoUserService.addFriend(id, friendId);
    }

    /**
     * 删除好友
     * @return 是否删除成功
     */
    @ApiOperation(value = "删除好友")
    @PostMapping("/del")
    public boolean delFriend(HttpSession session, @RequestBody FriendIdRequest body) {
        Integer id = (Integer) session.getAttribute(SessionConstant.USER_ID);
        if(id == null) return false;
        return mongoUserService.delFriend(id, body.getFriendId());
    }

    /**
     * 获取好友列表
     * @return 好友列表
     */
    @ApiOperation(value = "获取好友列表")
    @PostMapping("/get")
    public List<User> getFriend(HttpSession session) {
        Integer id = (Integer) session.getAttribute(SessionConstant.USER_ID);
        if(id == null) return List.of();
        return mongoUserService.getFriends(id);
    }

}
