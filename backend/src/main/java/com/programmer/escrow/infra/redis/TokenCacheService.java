package com.programmer.escrow.infra.redis;

import com.programmer.escrow.common.constant.RedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class TokenCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final Map<String, CacheEntry> localTokenCache = new ConcurrentHashMap<>();

    public TokenCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void storeUserToken(String token, Long userId, Integer userType, long expireSeconds, String nickname) {
        String payload = userId + ":" + userType;
        try {
            String tokenKey = String.format(RedisKeys.AUTH_TOKEN, token);
            stringRedisTemplate.opsForValue().set(tokenKey, payload, expireSeconds, TimeUnit.SECONDS);
            String userTokenKey = String.format(RedisKeys.AUTH_USER_TOKENS, userId);
            stringRedisTemplate.opsForSet().add(userTokenKey, token);
            stringRedisTemplate.expire(userTokenKey, expireSeconds, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeys.SESSION_USER, userId),
                    nickname,
                    30,
                    TimeUnit.MINUTES
            );
        } catch (Exception ex) {
            localTokenCache.put(String.format(RedisKeys.AUTH_TOKEN, token), new CacheEntry(payload, expireSeconds));
        }
    }

    public String getUserTokenPayload(String token) {
        return getPayload(String.format(RedisKeys.AUTH_TOKEN, token));
    }

    public void storeAdminToken(String token, Long adminId, String roleCode, long expireSeconds, String username) {
        String payload = adminId + ":" + roleCode;
        try {
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeys.ADMIN_AUTH_TOKEN, token),
                    payload,
                    expireSeconds,
                    TimeUnit.SECONDS
            );
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeys.ADMIN_SESSION, adminId),
                    username,
                    30,
                    TimeUnit.MINUTES
            );
        } catch (Exception ex) {
            localTokenCache.put(String.format(RedisKeys.ADMIN_AUTH_TOKEN, token), new CacheEntry(payload, expireSeconds));
        }
    }

    public String getAdminTokenPayload(String token) {
        return getPayload(String.format(RedisKeys.ADMIN_AUTH_TOKEN, token));
    }

    private String getPayload(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }
        CacheEntry cacheEntry = localTokenCache.get(key);
        if (cacheEntry == null) {
            return null;
        }
        if (cacheEntry.expireAtEpochSecond < Instant.now().getEpochSecond()) {
            localTokenCache.remove(key);
            return null;
        }
        return cacheEntry.value;
    }

    private static final class CacheEntry {
        private final String value;
        private final long expireAtEpochSecond;

        private CacheEntry(String value, long ttlSeconds) {
            this.value = value;
            this.expireAtEpochSecond = Instant.now().getEpochSecond() + ttlSeconds;
        }
    }
}
