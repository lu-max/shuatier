CREATE DATABASE IF NOT EXISTS ste_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ste_user;

-- 用户表，手机号作为唯一识别，同手机号视为同一用户
CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    open_id VARCHAR(64) DEFAULT NULL COMMENT '微信OpenId',
    union_id VARCHAR(64) DEFAULT NULL COMMENT '微信UnionId',
    nickname VARCHAR(64) NOT NULL COMMENT '昵称',
    avatar VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
    bio VARCHAR(512) DEFAULT NULL COMMENT '个人简介',
    real_name VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    source VARCHAR(32) NOT NULL DEFAULT 'PHONE' COMMENT '首次进入来源：PHONE/WECHAT',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_profile_phone (phone),
    UNIQUE KEY uk_user_profile_open_id (open_id),
    UNIQUE KEY uk_user_profile_union_id (union_id),
    KEY idx_user_profile_status (status)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户表';
