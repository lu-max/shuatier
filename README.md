# shuaitier

基于 Spring Boot 3.5 + Spring Cloud 的微服务练习项目。

## 模块说明

- `ste-gateway`：网关服务，负责统一入口和 JWT 鉴权
- `ste-user`：用户服务，负责登录、用户信息和 token 刷新
- `ste-common`：公共模块，提供通用常量、DTO、工具类
- `ste-question`：题目相关模块，当前预留
- `SQL`：数据库建表脚本

## 已实现功能

- Maven 多模块工程结构
- Spring Boot 3.5 / Spring Cloud 2025 基础依赖整合
- Gateway 网关转发
- JWT 登录鉴权
- Redis 会话存储
- AccessToken / RefreshToken 双 token 机制
- 登录、刷新 token、退出登录、获取当前登录用户接口
- 用户单表设计，使用手机号作为唯一识别
- 手机号登录
- 微信登录
- 同手机号自动识别为同一个用户
- 登录和注册合并处理，不存在用户时自动创建
- MyBatis-Plus 持久层集成
- Swagger / OpenAPI 接口文档支持

## 当前登录规则

- 用户可以使用手机号登录
- 用户可以使用微信登录
- 只要手机号相同，就判定为同一个用户
- 不单独区分登录和注册，系统会自动处理新增或登录

## 主要接口

- `POST /api/user/auth/login`
- `POST /api/user/auth/refresh`
- `POST /api/user/auth/logout`
- `GET /api/user/auth/me`

## 运行说明

- `ste-gateway` 默认端口：`8080`
- `ste-user` 默认端口：`8081`
- 需要本地准备 MySQL 和 Redis

