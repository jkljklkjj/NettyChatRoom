CREATE DATABASE IF NOT EXISTS user;

USE user;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'QQ号',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    password VARCHAR(10) NOT NULL COMMENT '密码',
    email VARCHAR(50) COMMENT '邮箱',
    phone CHAR(11) COMMENT '手机号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT '用户基本信息表';

Insert into users (username, password, email, phone) values
('admin', '123456', '2998568539@qq.com','13729047853')