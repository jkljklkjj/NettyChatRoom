package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性，可在 application.yml 中通过 jwt.* 进行覆盖
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /** 密钥 */
    private String secret = "change-me";
    /** 过期时间(毫秒) */
    private long expirationMs = 10 * 60 * 60 * 1000L; // 默认10小时
    /** 请求头名称 */
    private String header = "Authorization";
    /** Bearer 前缀 */
    private String prefix = "Bearer ";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
}
