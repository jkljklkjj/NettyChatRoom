# ChatRoom 项目概要

## 项目概述
这是一个基于Netty和Spring Boot的聊天室应用程序，支持单聊和群聊功能。项目使用WebSocket协议实现实时通信，并集成了Redis、MongoDB和MySQL等数据存储解决方案。

## 技术栈
- **核心框架**: Spring Boot 3.4.3
- **网络通信**: Netty 4.1.105.Final
- **数据库**: MySQL 8.0.33
- **缓存**: Redis
- **文档存储**: MongoDB
- **消息队列**: Kafka
- **安全认证**: JWT
- **ORM框架**: MyBatis-Plus 3.5.5
- **JSON处理**: FastJSON、Jackson

## 项目结构
```
src/main/java/com/example/
├── annotation/         # 自定义注解
├── aspect/             # AOP切面处理
├── common/             # 通用工具类和API封装
├── config/             # 配置类
├── constant/           # 常量定义
├── controller/         # REST控制器
├── handler/            # Netty处理器
│   ├── MessageHandlerImpl/  # 消息处理器实现
│   └── SessionManager.java  # 会话管理器
├── model/              # 数据模型
│   ├── mongo/          # MongoDB数据模型
│   └── mysql/          # MySQL数据模型
├── service/            # 业务逻辑层
│   ├── mongo/          # MongoDB服务
│   ├── mysql/          # MySQL服务
│   ├── netty/          # Netty服务
│   ├── redis/          # Redis服务
│   └── security/       # 安全服务
└── Application.java    # 应用启动类
```

## 核心组件

### 1. Netty服务器 (Server.java)
- 使用NioEventLoopGroup处理网络I/O
- 集成HTTP和WebSocket协议支持
- 包含心跳检测和超时处理机制
- 支持JWT身份验证

### 2. 消息处理机制
- **消息类型**: login, logout, chat, check, groupChat, heartbeat
- **处理器工厂**: MessageHandlerFactory动态创建对应的消息处理器
- **消息路由**: 根据消息类型分发到不同的处理器

### 3. 会话管理 (SessionManager.java)
- 管理用户在线状态
- 维护用户Channel映射关系
- 管理群组ChannelGroup
- 结合Redis记录用户在线状态
- 结合MongoDB管理群组成员关系

### 4. 数据存储
- **MySQL**: 存储用户信息、消息记录等结构化数据
- **Redis**: 记录用户在线状态、Token等缓存数据
- **MongoDB**: 存储群组信息等文档数据
- **Kafka**: 处理离线消息队列

## 通信协议

### WebSocket通信流程
1. 客户端发起WebSocket握手请求
2. 服务端验证JWT Token
3. 握手成功后建立WebSocket连接
4. 客户端发送JSON格式消息
5. 服务端根据消息类型分发处理
6. 实时推送消息给目标用户或群组

### 消息格式
```json
{
  "type": "chat",           // 消息类型
  "targetClientId": "123",  // 目标用户ID
  "content": "Hello!",      // 消息内容
  "UserId": "456",          // 发送者ID
  "timestamp": "2023-..."   // 时间戳
}
```

## 主要功能模块

### 用户管理
- 用户登录/登出
- 在线状态维护
- 用户会话管理

### 单聊功能
- 实时消息发送
- 离线消息存储
- 消息状态标记

### 群聊功能
- 群组创建和管理
- 群消息广播
- 群成员管理

### 安全机制
- JWT Token验证
- 请求权限控制
- 数据加密传输

## 配置文件
- **application.yml**: 应用配置文件，包含数据库连接、服务器端口等配置
- **logback.xml**: 日志配置
- **MyBatis映射文件**: 数据库操作映射

## 启动和部署
1. 启动依赖服务: Redis, MongoDB, MySQL, Kafka
2. 运行Spring Boot应用
3. Netty服务器默认监听8080端口
4. HTTP服务默认监听8088端口

## 测试
- 包含单元测试和集成测试
- 提供Netty客户端测试工具