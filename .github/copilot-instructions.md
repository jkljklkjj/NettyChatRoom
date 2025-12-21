# Copilot Instructions for NettyChatRoom

## 项目架构与核心组件

- **Spring Boot** 驱动，入口为 `com.example.Application`，集成 MyBatis-Plus、Netty、Kafka、Redis、MongoDB。
- **分层结构**：
  - `controller/`：REST API 层，处理前端请求，依赖 service。
  - `service/`：业务逻辑层，整合 repository，直接调用 mapper。
  - `repository/`：数据库操作封装（部分场景，MyBatis 主要用 mapper）。
  - `mapper/`：MyBatis 映射接口，操作 MySQL。
  - `model/`：数据库实体映射（`mysql/`、`mongo/`）。
  - `config/`：各类配置（如 Kafka、Redis、Security、WebMvc）。
  - `constant/`：常量定义，便于全局复用。
- **消息流转**：消息相关 API 见 `MessageController`，通过 `MessageService` 调用 `MessageMapper`，最终落库 MySQL。
- **MongoDB** 主要用于存储聊天记录、好友、群组等非结构化数据，结构详见 `readme/Mongo.md`。

## Netty 通信与消息分发

- **Netty 服务端**：`service.netty.Server` 实现 `SmartLifecycle`，随 Spring 启动自动监听端口（默认 8080，可通过 `netty.port` 配置），支持热启停，`netty.enabled` 控制是否启用。
- **协议与管道**：服务端 pipeline 依次注入 `IdleStateHandler`（心跳）、`TimeoutHandler`、HTTP 编解码、`JwtRequestHandler`（鉴权）、`HttpServerHandler`（业务分发）。
- **消息分发**：`HttpServerHandler` 解析 HTTP JSON 请求，根据 `type` 字段动态分发到 `MessageHandlerFactory`，支持单聊、群聊、登录、心跳等类型（详见 `ServiceConstant`）。
- **单聊/群聊处理**：
  - 单聊：`ChatHandler`，优先通过 `SessionManager` 查找目标用户在线 Channel，若不在线则通过 Kafka 发送离线消息（`KafkaProducer`）。
  - 群聊：`GroupChatHandler`，通过 `SessionManager` 获取群组 ChannelGroup，群发消息。
- **Session 管理**：`SessionManager` 维护用户 Channel 映射、群组 ChannelGroup，结合 Redis 标记在线状态，MongoDB 查询群组成员。
- **客户端示例**：`service.netty.Client`/`Client2` 提供命令行 Netty 客户端样例，便于本地调试。

## 关键开发流程

- **本地启动依赖**：
  - 执行 `scripts/start.bat`，自动分别启动 Kafka、MongoDB、Redis（需配置好本地路径）。
- **服务启动**：
  - 使用 `mvn spring-boot:run` 或 IDE 运行 `Application.java`。
- **数据库**：
  - MySQL 连接参数见 `application.yml`，表结构及测试数据见 `resources/sql/`。
- **配置文件**：
  - 主要配置集中于 `application.yml`，MyBatis 映射文件在 `resources/mapper/`。

## 项目约定与模式

- **注解**：如 `@RequireUserId` 用于接口鉴权，配合切面（`aspect/`）实现。
- **命名规范**：包名小写，类名驼峰，常量全大写下划线分隔。
- **事务**：业务层（service）方法如需事务，使用 `@Transactional` 注解。
- **日志**：日志配置见 `logback.xml`，可通过 `application.yml` 控制日志级别。
- **异步**：全局启用 `@EnableAsync`，可在 service 层使用 `@Async`。

## 典型交互示例

- 获取离线消息：`/message/getOfflineMessage`，需携带 `UserId`。
- 消息落库：`MessageService.insertMessage()` → `MessageMapper.insertMessage()`。
- Netty 单聊/群聊：HTTP POST JSON，type 字段为 `chat` 或 `groupChat`，参考 `ServiceConstant`。

## 其他说明

- **代码质量**：CI/CD 使用 Qodana（见 `qodana.yaml`），JDK 17。
- **扩展点**：如需新增消息类型、存储方式，建议遵循现有分层与命名规范。

---
如需补充或澄清，请在下方留言。
