package com.example.service.mysql;

import com.example.mapper.UserMapper;
import com.example.model.mysql.User;
import com.example.service.redis.RedisService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.util.JwtUtil;

@Service
public class UserService {
    private UserMapper userMapper;
    private final String REDIS_KEY_PREFIX = "user:";
    private final RedisService jedis;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, RedisService redisService, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jedis = redisService;
        this.jwtUtil = jwtUtil;
    }

    public int register(User user) {
        System.out.println("有顾客正在注册");
        System.out.println(user.getUsername());
        if (userMapper.selectUserByName(user.getUsername()) != null) {
            return -1;
        }
        return userMapper.insertUser(user);
    }

    public String login(int id, String password, HttpServletRequest request) {
        System.out.println("用户登录中...");
        boolean res = authenticateUser(id, password);
        if (res) {
            String ipAddress = request.getRemoteAddr();
            jedis.set("user:" + id + "ip", ipAddress);
            String result = (String) jedis.get("user:" + id);
            System.out.println("用户" + id + "登录成功，IP地址为：" + result);
            // 生成token
            String token = jwtUtil.generateToken(String.valueOf(id));
            System.out.println("用户" + id + "的token" + token);
            jedis.set(token, id, 518400, TimeUnit.SECONDS);
            return token;
        }
        return "";
    }

    public boolean authenticateUser(int id, String password) {
        User user = userMapper.selectUser(id);
        return user != null && user.getPassword().equals(password);
    }

    public User getUserById(int id) {
        if (jedis.exists(REDIS_KEY_PREFIX + id)) {
            return (User) jedis.get(REDIS_KEY_PREFIX + id);
        }
        return userMapper.selectUser(id);
    }
}