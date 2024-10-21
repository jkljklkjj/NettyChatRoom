package com.example.model;

public class Msg {
    private String type;
    private String targetClientId;
    private String content;

    public Msg(String type, String targetClientId, String content) {
        this.type = type;
        this.targetClientId = targetClientId;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getTargetClientId() {
        return targetClientId;
    }

    public String getContent() {
        return content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTargetClientId(String targetClientId) {
        this.targetClientId = targetClientId;
    }
}
