package com.example.controller;

import com.example.constant.SessionConstant;
import com.example.dto.LoginRequest;
import com.example.dto.EmailLoginRequest;
import com.example.dto.TokenValidateRequest;
import com.example.dto.AddGroupRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.api.ApiResponse;
import com.example.common.api.BusinessException;
import com.example.common.api.ErrorCode;
import com.example.model.mongo.MongoUser;
import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;
import com.example.service.redis.RedisService;
import com.example.service.security.JwtService;

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
    private final JwtService jwtService;
    private final RedisService jedis;

    public UserController(
            UserService userService,
            MongoUserService mongoUserService,
            JwtService jwtService,
            RedisService redisTemplate) {
        this.jedis = redisTemplate;
        this.userService = userService;
        this.mongoUserService = mongoUserService;
        this.jwtService = jwtService;
    }

    /**
     * 根据用户 ID 获取Mysql用户信息
     * @return 用户的映射
     */
    @ApiOperation(value = "获取用户信息")
    @GetMapping("/get")
    public ApiResponse<User> getUserById(HttpSession session) {
        Integer id = (Integer) session.getAttribute(SessionConstant.USER_ID);
        if (id == null) throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        User u = userService.getUserById(id);
        if (u == null) throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        return ApiResponse.success(u);
    }

    /**
     * 根据用户 ID 获取Mongo用户信息
     * @param id 用户在Mysql自动生成的id
     * @return 用户的映射
     */
    @ApiOperation(value = "获取Mongo用户信息")
    @GetMapping("/mongo/{id}")
    public ApiResponse<MongoUser> getMongoUserByUserId(@PathVariable(name = "id") int id) {
        MongoUser u = mongoUserService.getUserByUserId(id);
        if (u == null) throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        return ApiResponse.success(u);
    }

    /**
     * 登录
     * @return 是否登录成功
     */
    @ApiOperation(value = "登录")
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String token = userService.login(loginRequest.getId(), loginRequest.getPassword(), request);
        if (token == null || token.isEmpty()) throw new BusinessException(ErrorCode.LOGIN_FAIL, "账号或密码错误");
        jedis.set(token, String.valueOf(loginRequest.getId()), 7 * 24 * 60 * 60);
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConstant.USER_ID, loginRequest.getId());
        return ApiResponse.success(token);
    }

    /**
     * 根据邮箱和密码登录
     * @return 是否登录成功
     */
    @ApiOperation(value = "根据用户名和密码登录")
    @PostMapping("/loginByEmail")
    public ApiResponse<String> loginByName(@RequestBody EmailLoginRequest emailLoginRequest, HttpServletRequest request) {
        User user = userService.getUserByEmail(emailLoginRequest.getEmail());
        if (user == null) throw new BusinessException(ErrorCode.LOGIN_FAIL, "用户不存在");
        String token = userService.login(user.getId(), emailLoginRequest.getPassword(), request);
        if (token == null || token.isEmpty()) throw new BusinessException(ErrorCode.LOGIN_FAIL, "账号或密码错误");
        return ApiResponse.success(token);
    }

    @PostMapping("/validate")
    public ApiResponse<Boolean> validate(@RequestBody TokenValidateRequest req){
        return ApiResponse.success(jwtService.validate(req.getToken()));
    }

    @ApiOperation(value = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(HttpSession session){
        Integer id = (Integer) session.getAttribute(SessionConstant.USER_ID);
        if(id != null){
            jedis.del("user:"+id);
        }
        session.invalidate();
        return ApiResponse.success(true);
    }

    /**
     * 注册
     * @param user 用户信息
     * @return 是否注册成功
     */
    @ApiOperation(value = "注册")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register")
    public ApiResponse<Integer> register(@ApiParam("用户信息") @RequestBody User user) {
        int beforeId = user.getId();
        userService.register(user);
        int id = user.getId() == 0 ? beforeId : user.getId();
        MongoUser mongoUser = new MongoUser(id, null, null);
        if(!mongoUserService.register(mongoUser)){
            throw new BusinessException(ErrorCode.REGISTER_FAIL, "MongoDB register failed");
        }
        return ApiResponse.success(id);
    }

    @ApiOperation(value = "添加群组")
    @PostMapping("/addgroup")
    public ApiResponse<Boolean> addGroup(HttpSession session, @RequestBody AddGroupRequest request) {
        Integer id = (Integer) session.getAttribute(SessionConstant.USER_ID);
        if (id == null) throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        return ApiResponse.success(mongoUserService.addGroup(id, request.getGroupId()));
    }
}
