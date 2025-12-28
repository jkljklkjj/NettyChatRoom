create table messages
(
    id           bigint auto_increment comment '消息主键'
        primary key,
    sender_id    varchar(64)                          not null comment '发送者ID',
    receiver_id  varchar(64)                          not null comment '接收者ID',
    message      text                                 not null comment '消息内容',
    is_received  tinyint(1) default 0                 not null comment '是否已接收',
    timestamp    timestamp  default CURRENT_TIMESTAMP not null comment '发送时间',
    receive_time datetime                             null comment '接收时间'
)
    comment '消息表';

create index idx_receiver_isreceived
    on messages (receiver_id, is_received);

create index idx_sender_sendtime
    on messages (sender_id, timestamp);