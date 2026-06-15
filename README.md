# 太理朋友圈

> 太原理工大学聊天平台 · 课程设计项目

一个仿微信的网页聊天系统，支持私聊、群聊、音视频通话等核心 IM 功能，可用于校园内的即时通讯。

## 功能特性

- 私聊、群聊、离线消息
- 发送语音、图片、文件
- 已读未读、群 @ 提及
- 一对一音视频通话（基于原生 WebRTC，不依赖收费第三方 SDK）
- Web 端，多窗口同时在线，消息同步

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3 + Netty + MyBatis-Plus |
| 前端 Web | Vue 2 + Element UI |
| 存储 | MySQL 8.0 + Redis + MinIO |

## 项目结构

| 模块 | 功能 |
|---|---|
| chat-platform | 业务平台服务，处理用户业务请求（HTTP） |
| chat-server | 消息推送服务，将消息推送给用户（WebSocket） |
| chat-client | 消息推送 SDK，供其他服务集成与 chat-server 通信 |
| chat-common | 公共包，后端服务均依赖 |
| chat-web | Web 前端 |

## 消息推送集群化方案

当消息的发送者和接收者连接的不是同一个 chat-server 时，消息无法直接推送，因此设计了支持跨节点推送的方案：

- 利用 Redis 的 List 数据结构实现消息推送，key 为 `im:unread:${serverid}`，每个 key 可看作一个 queue，每个 chat-server 根据自身 id 只消费属于自己的 queue
- Redis 记录每个用户的 WebSocket 连接的是哪个 chat-server；当用户发送消息时，chat-platform 根据接收方连接的 chat-server 的 id，决定将消息推向哪个 queue

服务器支持集群化部署，具有良好的横向扩展能力。

## 本地启动

### 1. 安装运行环境

- Node.js：v18.19.0
- JDK：17
- Maven：3.9.6
- MySQL：8.0（账号密码 root/root，创建名为 `im_platform_open` 的数据库，导入 `db/im-platform.sql` 脚本）
- Redis：6.2
- MinIO：使用默认账号、密码、端口

### 2. 启动后端服务

```bash
mvn clean package
java -jar ./chat-platform/target/chat-platform.jar
java -jar ./chat-server/target/chat-server.jar
```

### 3. 启动前端 Web

```bash
cd chat-web
npm install
npm run serve
```

访问 http://localhost:8080
