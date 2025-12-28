package com.example.service.redis;

import com.example.model.mysql.Message;
import com.example.service.mysql.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.Map;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;

@Component
public class OfflineMessagePersister {
    private static final Logger logger = LoggerFactory.getLogger(OfflineMessagePersister.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RedisOfflineService redisOfflineService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final MessageService messageService;

    public OfflineMessagePersister(RedisOfflineService redisOfflineService,
                                   org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate,
                                   MessageService messageService) {
        this.redisOfflineService = redisOfflineService;
        this.redisTemplate = redisTemplate;
        this.messageService = messageService;
    }

    // 每分钟扫描一次到期的成员并持久化（可在 application.yml 中调整）
    @Scheduled(fixedDelayString = "${offline.persist.delay.millis:60000}")
    public void persistExpired() {
        final long now = System.currentTimeMillis();

        // 使用 RedisTemplate.scan 流式获取匹配键，避免使用低层 connection.scan 的 deprecated API
        ScanOptions options = ScanOptions.scanOptions().match("offline:expiry:*").count(500L).build();
        Cursor<String> cursor = redisTemplate.scan(options);
        try {
            while (cursor.hasNext()) {
                String zsetKey = cursor.next();
                if (zsetKey == null || zsetKey.isEmpty()) continue;

                Set<Object> members = redisTemplate.opsForZSet().rangeByScore(zsetKey, 0, now);
                if (members == null || members.isEmpty()) continue;

                for (Object m : members) {
                    try {
                        String member = String.valueOf(m);
                        Map wrapper = MAPPER.readValue(member, Map.class);
                        if (wrapper == null) continue;
                        Map payload = (Map) wrapper.get("payload");
                        if (payload == null) continue;

                        String receiverId = String.valueOf(payload.get("receiverId"));
                        String senderId = String.valueOf(payload.get("senderId"));
                        String messageText = String.valueOf(payload.get("message"));
                        long timestamp = 0L;
                        Object tsObj = payload.get("timestamp");
                        if (tsObj instanceof Number) timestamp = ((Number) tsObj).longValue();
                        else if (tsObj != null) timestamp = Long.parseLong(String.valueOf(tsObj));

                        Message msg = new Message();
                        msg.setReceiverId(receiverId);
                        msg.setSenderId(senderId);
                        msg.setMessage(messageText);
                        msg.setTimestamp(new Date(timestamp));

                        try {
                            messageService.insertMessage(msg);
                        } catch (Exception e) {
                            logger.error("Failed to insert message to DB", e);
                            continue;
                        }

                        // 从 zset 中移除 member
                        redisTemplate.opsForZSet().remove(zsetKey, member);

                        // 尝试从 list 中移除相同的 payload（避免用户登录时重复收到）
                        String listKey = "offline:msg:" + receiverId;
                        String payloadJson = MAPPER.writeValueAsString(payload);
                        redisTemplate.opsForList().remove(listKey, 0, payloadJson);
                    } catch (Exception e) {
                        logger.error("Failed to persist offline message member", e);
                    }
                }
            }
        } finally {
            try { cursor.close(); } catch (Exception ignore) {}
        }
    }
}
