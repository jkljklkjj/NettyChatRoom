package com.example.service.mysql;

import com.alibaba.fastjson.JSON;
import com.example.constant.RedisPrefixConstant;
import com.example.mapper.UserMapper;
import com.example.model.mysql.User;
import com.example.service.redis.RedisService;
import com.example.service.security.JwtService; // 新增

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService jedis;

    @Autowired
    private JwtService jwtService; // 注入 JwtService

    public int register(User user) {
        System.out.println("有顾客正在注册");
        System.out.println(user.getUsername());
//        if (userMapper.selectUserByName(user.getUsername()) != null) {
//            System.out.println("用户已存在");
//            return -1;
//        }
        return userMapper.insertUser(user);
    }

    public String login(int id, String password, HttpServletRequest request) {
        System.out.println("用户登录中...");
        boolean res = authenticateUser(id, password);
        if (res) {
            String ipAddress = request.getRemoteAddr();
            jedis.set("user:" + id + "ip", ipAddress);
            String result = (String) jedis.get("user:" + id+ "ip");
            System.out.println("用户" + id + "登录成功，IP地址为：" + result);
            // 生成token (改用 JwtService)
            String token = jwtService.generateToken(id);
            System.out.println("用户" + id + "的token" + token);
            jedis.set(token, String.valueOf(id), 518400, TimeUnit.SECONDS);
            return token;
        }
        return "";
    }

    public boolean authenticateUser(int id, String password) {
        User user = userMapper.selectUser(id);
        return user != null && user.getPassword().equals(password);
    }

    public User getUserById(int id) {
        if (jedis.exists(RedisPrefixConstant.USER_REDIS_KEY_PREFIX + id)) {
            return JSON.parseObject(jedis.get(RedisPrefixConstant.USER_REDIS_KEY_PREFIX + id),User.class);
        }
        User user = userMapper.selectUser(id);
        System.out.println("从数据库中获取用户信息"+user.getUsername());
        jedis.set(RedisPrefixConstant.USER_REDIS_KEY_PREFIX + id, JSON.toJSONString(user), 518400, TimeUnit.SECONDS);
        return user;
    }

    public User getUserByEmail(String email) {
        if (jedis.exists(RedisPrefixConstant.USER_REDIS_KEY_PREFIX + email)) {
            return JSON.parseObject(jedis.get(RedisPrefixConstant.USER_REDIS_KEY_PREFIX + email),User.class);
        }
        User user = userMapper.selectUserByEmail(email);
        System.out.println("从数据库中获取用户信息"+user.getUsername());
        jedis.set(RedisPrefixConstant.USER_REDIS_KEY_PREFIX + email, JSON.toJSONString(user), 518400, TimeUnit.SECONDS);
        return user;
    }
}