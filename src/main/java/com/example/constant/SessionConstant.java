package com.example.constant;

/**
 * Session存储对应key
 */
public class SessionConstant {
    /**
     * session存储前缀
     */
    public static final String SESSION_PREFIX = "session:";

    /**
     * session过期时间，单位秒，7天
     */
    public static final int SESSION_EXPIRE = 7 * 24 * 60 * 60;

    public static final String USER_ID = SESSION_PREFIX+"userId";
}
