package com.example.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class Message {
    @JsonIgnore
    private String id;//唯一标识
    private String senderId;//发送者账号
    private String message;//消息内容
    private String receiverId;//接收者账号
    private Date timestamp;//时间戳，改为Date类型

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
