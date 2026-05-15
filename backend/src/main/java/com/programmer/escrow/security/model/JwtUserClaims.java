package com.programmer.escrow.security.model;

import java.time.Instant;
import java.util.List;

public class JwtUserClaims {

    private final String jti;
    private final Long userId;
    private final Integer userType;
    private final String nickname;
    private final List<String> roles;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public JwtUserClaims(String jti,
                         Long userId,
                         Integer userType,
                         String nickname,
                         List<String> roles,
                         Instant issuedAt,
                         Instant expiresAt) {
        this.jti = jti;
        this.userId = userId;
        this.userType = userType;
        this.nickname = nickname;
        this.roles = roles;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public String jti() {
        return jti;
    }

    public Long userId() {
        return userId;
    }

    public Integer userType() {
        return userType;
    }

    public String nickname() {
        return nickname;
    }

    public List<String> roles() {
        return roles;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
