package com.example.controller;

import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;
import com.example.service.redis.RedisService;
import com.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {
    private final UserService userService;
    private final MongoUserService mongoUserService;

    @Autowired
    private RedisService jedis;
    @Autowired
    private JwtUtil jwtUtil;

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
    @PostMapping("/del")
    public boolean delFriend(@RequestAttribute("UserId") int id, @RequestParam(name = "friendId", required = true) int friendId) {
        return mongoUserService.delFriend(id, friendId);
    }

    /**
     * 获取好友列表
     * @param id 用户 ID
     * @return 好友列表
     */
    @PostMapping("/get")
    public List<User> getFriend(@RequestAttribute("UserId") int id) {
        return mongoUserService.getFriends(id);
    }

}
