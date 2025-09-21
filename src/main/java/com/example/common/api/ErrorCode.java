package com.example.common.api;

public enum ErrorCode {
    SUCCESS(0, "OK"),
    UNAUTHORIZED(401, "未授权"),
    VALIDATION_ERROR(1001, "参数校验失败"),
    AUTH_FAILED(1002, "认证失败"),
    NOT_FOUND(1003, "资源不存在"),
    DUPLICATE(1004, "资源已存在"),
    FORBIDDEN(1005, "无权限"),
    SERVER_ERROR(1500, "服务器内部错误"),
    DATA_INCONSISTENT(1600, "数据状态不一致"),
    LOGIN_FAIL(2001, "登录失败"),
    REGISTER_FAIL(2002, "注册失败"),
    GROUP_NOT_FOUND(3001, "群组不存在"),
    FRIEND_OP_FAIL(4001, "好友操作失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

