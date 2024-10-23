package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.mongo.MongoUser;
import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;
import com.example.service.redis.RedisService;
import com.example.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户相关接口
 * 提供了登录注册，添加好友和群组，获取用户信息等接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final MongoUserService mongoUserService;

    @Autowired
    private RedisService jedis;
    @Autowired
    private JwtUtil jwtUtil;

    public UserController(UserService userService,MongoUserService mongoUserService) {
        this.userService = userService;
        this.mongoUserService = mongoUserService;
    }

    /**
     * 根据用户 ID 获取Mysql用户信息
     * @param id 用户在Mysql自动生成的id
     * @return 用户的映射
     */
    @PostMapping("/get")
    public User getUserById(@RequestAttribute("UserId") int id) {
        return userService.getUserById(id);
    }

    /**
     * 根据用户 ID 获取Mongo用户信息
     * @param id 用户在Mysql自动生成的id
     * @return 用户的映射
     */
    @GetMapping("/mongo/{id}")
    public MongoUser getMongoUserByUserId(@PathVariable(name = "id", required = true) int id) {
        return mongoUserService.getUserByUserId(id);
    }

    /**
     * 登录
     * @param id 用户 ID
     * @param password 密码
     * @return 是否登录成功
     */
    @PostMapping("/login")
    public String login(@RequestParam int id, @RequestParam String password,HttpServletRequest request) {
        System.out.println("用户"+id+"登录中...");
        return userService.login(id, password,request);
    }

    @PostMapping("/validate")
    public boolean validate(@RequestParam String token){
        return jwtUtil.validateToken(token, jwtUtil.extractClaims(token).getSubject());
    }
    

    @PostMapping("/logout")
    public boolean logout(@RequestAttribute("UserId") int id){
        jedis.del("user:"+id);
        System.out.println("用户"+id+"已退出登录");
        return true;
    }

    /**
     * 注册
     * @param user 用户信息
     * @return 是否注册成功
     */
    @PostMapping("/register")
    public int register(@RequestBody User user) {
        System.out.println("用户注册中...");
        userService.register(user);
        int id = user.getId();
        MongoUser mongoUser = new MongoUser(id, null, null);
        System.out.println(id+"用户注册成功！");
        if(!mongoUserService.register(mongoUser)){
            return -1;
        }
        return id;
    }

    /**
     * 添加群组
     * @param id 用户 ID
     * @param groupid 群组 ID
     * @return 是否添加成功
     */
    @PostMapping("/addgroup")
    public boolean addGroup(@RequestAttribute("UserId") int id, @RequestParam int groupid) {
        return mongoUserService.addGroup(id, groupid);
    }

}
