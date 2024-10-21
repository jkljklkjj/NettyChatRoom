package com.example.model.mysql;

public class Group {
    private int id;// 群聊ID
    private String name;// 群聊名称
    private String description;// 群聊描述

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Group() {
        this.name = "";
        this.description = "";
        this.id = -1;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
