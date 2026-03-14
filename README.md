# shuaitier

基于 Spring Boot 3.5 和 Spring Cloud 的微服务练习项目。

## 模块说明

- `ste-gateway`：网关服务，负责统一入口、JWT 校验和请求转发
- `ste-user`：用户服务，负责登录、token 刷新、退出登录和当前用户信息
- `ste-common`：公共模块，提供常量、DTO、工具类等通用能力
- `ste-question`：题目相关模块，当前为预留模块
- `SQL`：数据库建表脚本

## 已实现功能

- Maven 多模块项目结构
- Spring Boot 3.5 / Spring Cloud 2025 依赖整合
- Gateway 网关转发
- JWT 鉴权
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

## 登录选择设计

登录方式选择使用了工厂模式和策略模式组合实现。

### 工厂模式

- `AuthFactory` 作为登录工厂
- 根据前端传入的 `loginType` 选择对应的登录处理器
- 屏蔽控制层对具体登录实现的依赖

### 策略模式

- `AuthGranter` 作为登录策略统一接口
- `SmsGranter` 负责手机号登录
- `WeChatGranter` 负责微信登录
- `AccountGranter` 作为账号密码登录的预留策略

### 当前调用流程

- `AuthController` 接收登录请求
- `AuthService` 调用 `AuthFactory`
- `AuthFactory` 根据 `loginType` 选择具体 `AuthGranter`
- 对应策略再调用用户身份服务完成实际登录逻辑

这种实现方式的好处是：
- 开放封闭原则
- 新增登录方式时只需要增加新的策略类
- 控制层不需要修改分支判断
- 登录扩展点清晰，后续接短信验证码、微信授权、账号密码都更方便

## 当前登录规则

- 用户可以使用手机号登录
- 用户可以使用微信登录
- 不单独区分登录和注册，系统会自动处理新增或登录

## 主要接口

- `POST /api/user/auth/login`
- `POST /api/user/auth/refresh`
- `POST /api/user/auth/logout`
- `GET /api/user/auth/me`

## 运行说明

- `ste-gateway` 默认端口：`8080`
- `ste-user` 默认端口：`8081`
- 本地需要准备 MySQL 和 Redis

