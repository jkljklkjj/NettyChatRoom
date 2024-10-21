CREATE TABLE `group_users` (
    group_id INT NOT NULL COMMENT '群号',
    user_id INT NOT NULL COMMENT '用户ID',
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES `groups`(id),
    FOREIGN KEY (user_id) REFERENCES `users`(id)
);