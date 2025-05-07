package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户相关接口
 * 提供了登录注册，添加好友和群组，获取用户信息等接口
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final MongoUserService mongoUserService;

    @Autowired
    private RedisService jedis;

    public UserController(UserService userService,MongoUserService mongoUserService) {
        this.userService = userService;
        this.mongoUserService = mongoUserService;
    }

    /**
     * 根据用户 ID 获取Mysql用户信息
     * @param id 用户在Mysql自动生成的id
     * @return 用户的映射
     */
    @ApiOperation(value = "获取用户信息")
    @GetMapping("/get")
    public User getUserById(@RequestAttribute("UserId") int id) {
        return userService.getUserById(id);
    }

    /**
     * 根据用户 ID 获取Mongo用户信息
     * @param id 用户在Mysql自动生成的id
     * @return 用户的映射
     */
    @ApiOperation(value = "获取Mongo用户信息")
    @GetMapping("/mongo/{id}")
    public MongoUser getMongoUserByUserId(@PathVariable(name = "id", required = true) int id) {
        return mongoUserService.getUserByUserId(id);
    }

    /**
     * 根据邮箱和密码登录
     * @param emailString 邮箱
     * @param password 密码
     * @return 是否登录成功
     */
    @ApiOperation(value = "根据用户名和密码登录")
    @PostMapping("/login")
    public String loginByName(@ApiParam("用户名") @RequestParam String emailString, @ApiParam("密码") @RequestParam String password,HttpServletRequest request) {
        System.out.println("邮箱为"+emailString+"的用户登录中...");

        User user = userService.getUserByEmail(emailString);

        return userService.login(user.getId(), password,request);

    }
    
    @PostMapping("/validate")
    public boolean validate(@RequestParam String token){
        return JwtUtil.validateToken(token, JwtUtil.extractClaims(token).getSubject());
    }
    
    @ApiOperation(value = "退出登录")
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
    @ApiOperation(value = "注册")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register")
    public int register(@ApiParam("用户信息") @RequestBody User user) {
        System.out.println("用户注册中...");
        userService.register(user);
        int id = user.getId();
        MongoUser mongoUser = new MongoUser(id, null, null);
        System.out.println(id+"用户注册成功！");
        if(!mongoUserService.register(mongoUser)){
            throw new RuntimeException("MongoDB register failed");
        }
        return id;
    }

    /**
     * 添加群组
     * @param id 用户 ID
     * @param groupId 群组 ID
     * @return 是否添加成功
     */
    @ApiOperation(value = "添加群组")
    @PostMapping("/addgroup")
    public boolean addGroup(@RequestAttribute("UserId") int id, @RequestParam int groupId) {
        return mongoUserService.addGroup(id, groupId);
    }

}
