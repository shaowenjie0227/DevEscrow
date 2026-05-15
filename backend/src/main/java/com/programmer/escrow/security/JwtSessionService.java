package com.programmer.escrow.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.escrow.common.constant.RedisKeys;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.security.model.LoginUserCache;
import com.programmer.escrow.user.entity.UserEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class JwtSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public JwtSessionService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void storeLoginUser(UserEntity user, IssuedJwtToken issuedJwtToken) {
        storeLoginUserSnapshot(
                user.getId(),
                user.getUserType(),
                user.getNickname(),
                user.getAvatarUrl(),
                issuedJwtToken.roles(),
                issuedJwtToken.jti(),
                Duration.between(java.time.Instant.now(), issuedJwtToken.expiresAt()).getSeconds(),
                issuedJwtToken.expiresAt()
        );
    }

    public void storeLoginUserSnapshot(Long userId,
                                       Integer userType,
                                       String nickname,
                                       String avatarUrl,
                                       List<String> roles,
                                       String jti,
                                       long ttlSeconds,
                                       java.time.Instant expiresAt) {
        LoginUserCache cache = LoginUserCache.builder()
                .userId(userId)
                .userType(userType)
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .roles(roles)
                .currentJti(jti)
                .expiresAt(expiresAt)
                .build();
        try {
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeys.LOGIN_USER, userId),
                    objectMapper.writeValueAsString(cache),
                    ttlSeconds,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize login user cache", ex);
        }
    }

    public LoginUserCache getLoginUser(Long userId) {
        String json = stringRedisTemplate.opsForValue().get(String.format(RedisKeys.LOGIN_USER, userId));
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, LoginUserCache.class);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to deserialize login user cache", ex);
        }
    }

    public void blacklistToken(String token, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(String.format(RedisKeys.JWT_BLACKLIST, token), "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(String.format(RedisKeys.JWT_BLACKLIST, token)));
    }
}
