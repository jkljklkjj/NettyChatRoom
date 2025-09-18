package com.example.service.mysql;

import com.alibaba.fastjson.JSON;
import com.example.constant.RedisPrefixConstant;
import com.example.mapper.UserMapper;
import com.example.model.mysql.User;
import com.example.service.redis.RedisService;
import com.example.service.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService jedis;

    @Autowired
    private JwtService jwtService; // 注入 JwtService

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码加密器

    public int register(User user) {
        log.info("注册用户: username={} id={}", user.getUsername(), user.getId());
        // 加密密码（若尚未加密）
        if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userMapper.insertUser(user);
    }

    public String login(int id, String password, HttpServletRequest request) {
        log.info("用户登录尝试 id={}", id);
        boolean res = authenticateUser(id, password);
        if (res) {
            String ipAddress = request.getRemoteAddr();
            jedis.set("user:" + id + "ip", ipAddress);
            String result = (String) jedis.get("user:" + id+ "ip");
            log.info("用户{} 登录成功, IP={}", id, result);
            String token = jwtService.generateToken(id);
            log.debug("用户{} token={}", id, token);
            jedis.set(token, String.valueOf(id), 518400, TimeUnit.SECONDS);
            return token;
        }
        log.warn("用户{} 登录失败: 凭证不匹配", id);
        return "";
    }

    public boolean authenticateUser(int id, String rawPassword) {
        User user = userMapper.selectUser(id);
        if (user == null) {
            log.warn("认证失败: 用户不存在 id={}", id);
            return false;
        }
        String stored = user.getPassword();
        if (stored == null) {
            log.warn("认证失败: 用户密码为空 id={}", id);
            return false;
        }
        // 兼容：如果已是BCrypt哈希走 matches，否则走明文比较（后续可迁移重哈希）
        boolean match = stored.startsWith("$2") ? passwordEncoder.matches(rawPassword, stored) : stored.equals(rawPassword);
        if (!match) {
            log.info("用户{} 密码不匹配", id);
        }
        return match;
    }

    public User getUserById(int id) {
        String key = RedisPrefixConstant.USER_REDIS_KEY_PREFIX + id;
        if (jedis.exists(key)) {
            return JSON.parseObject(jedis.get(key),User.class);
        }
        User user = userMapper.selectUser(id);
        if (user != null) {
            log.debug("从数据库加载用户 id={} username={}", id, user.getUsername());
            jedis.set(key, JSON.toJSONString(user), 518400, TimeUnit.SECONDS);
        }
        return user;
    }

    public User getUserByEmail(String email) {
        String key = RedisPrefixConstant.USER_REDIS_KEY_PREFIX + email;
        if (jedis.exists(key)) {
            return JSON.parseObject(jedis.get(key),User.class);
        }
        User user = userMapper.selectUserByEmail(email);
        if (user != null) {
            log.debug("从数据库加载用户 email={} username={}", email, user.getUsername());
            jedis.set(key, JSON.toJSONString(user), 518400, TimeUnit.SECONDS);
        }
        return user;
    }
}