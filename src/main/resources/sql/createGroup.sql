CREATE TABLE `groups` (
    id INT AUTO_INCREMENT PRIMARY KEY comment '群号',
    name VARCHAR(20) NOT NULL comment '群名称',
    description TEXT comment '描述信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);