package com.example.service.redis;

import com.example.model.mysql.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RedisOfflineService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RedisTemplate<String, Object> redisTemplate;

    // 保留期（毫秒），过期后会由 persister 持久化到 DB
    private final long retentionMillis;

    public RedisOfflineService(RedisTemplate<String, Object> redisTemplate,
                               @Value("${offline.retention.millis:604800000}") long retentionMillis) {
        this.redisTemplate = redisTemplate;
        this.retentionMillis = retentionMillis;
    }

    public void saveOfflineMessage(String receiverId, String senderId, String content, long timestamp) {
        try {
            // 构造 payload（存入 List），以及带 uuid 的 wrapper（存入 ZSET 用于延迟持久化）
            Map<String, Object> payload = Map.of(
                    "receiverId", receiverId,
                    "senderId", senderId,
                    "message", content,
                    "timestamp", timestamp
            );

            String payloadStr = MAPPER.writeValueAsString(payload);

            String listKey = "offline:msg:" + receiverId;
            // push 到 list，并设置 TTL
            redisTemplate.opsForList().leftPush(listKey, payloadStr);
            redisTemplate.expire(listKey, Duration.ofMillis(retentionMillis));

            // wrapper 包含 uuid，便于在 ZSET 中唯一标识
            Map<String, Object> wrapper = Map.of(
                    "id", UUID.randomUUID().toString(),
                    "payload", payload
            );

            String member = MAPPER.writeValueAsString(wrapper);
            // 使用按用户分片的 ZSET，降低单键扫描开销
            String zsetKey = "offline:expiry:" + receiverId;
            double score = (double) (System.currentTimeMillis() + retentionMillis);
            redisTemplate.opsForZSet().add(zsetKey, member, score);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 list 中读取并清空某用户离线消息（用户登录时调用）
     */
    public List<String> fetchAndClear(String receiverId) {
        String listKey = "offline:msg:" + receiverId;
        List<Object> range = redisTemplate.opsForList().range(listKey, 0, -1);

        List<String> results = new ArrayList<>();
        if (range == null || range.isEmpty()) return results;
        for (Object o : range) {
            results.add(String.valueOf(o));
        }

        redisTemplate.delete(listKey);
        return results;
    }

    /**
     * 分页弹出并清空 Redis 中的离线消息（每次最多 pop limit 条），并返回已转换的 Message 列表。
     * 同时尝试从对应用户的 ZSET 中移除已清空的成员以避免重复持久化。
     */
    public List<Message> fetchAndClearPaginated(String receiverId, int limit) {
        String listKey = "offline:msg:" + receiverId;
        String zsetKey = "offline:expiry:" + receiverId;
        List<Message> results = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Object popped = redisTemplate.opsForList().rightPop(listKey);
            if (popped == null) break;
            String payloadStr = String.valueOf(popped);
            try {
                Map<String, Object> map = MAPPER.readValue(payloadStr, Map.class);
                Object tsObj = map.get("timestamp");
                long ts = 0L;
                if (tsObj instanceof Number) ts = ((Number) tsObj).longValue();
                else if (tsObj != null) ts = Long.parseLong(String.valueOf(tsObj));

                Message m = new Message();
                m.setReceiverId(String.valueOf(map.get("receiverId")));
                m.setSenderId(String.valueOf(map.get("senderId")));
                m.setMessage(String.valueOf(map.get("message")));
                m.setTimestamp(new Date(ts));
                results.add(m);

                // 尝试移除 zset 中对应的 member（遍历 zset 成员并匹配 payload）
                try {
                    Set<Object> zmembers = redisTemplate.opsForZSet().range(zsetKey, 0, -1);
                    if (zmembers != null && !zmembers.isEmpty()) {
                        for (Object mem : zmembers) {
                            String memStr = String.valueOf(mem);
                            try {
                                Map wrapper = MAPPER.readValue(memStr, Map.class);
                                Object payload = wrapper.get("payload");
                                String payloadJson = MAPPER.writeValueAsString(payload);
                                if (payloadJson.equals(payloadStr)) {
                                    redisTemplate.opsForZSet().remove(zsetKey, memStr);
                                    break;
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                } catch (Exception ignored) {}

            } catch (Exception e) {
                // 解析失败则跳过
            }
        }

        return results;
    }
}
