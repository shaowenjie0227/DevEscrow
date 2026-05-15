package com.programmer.escrow.security.model;

import java.time.Instant;
import java.util.List;

public class IssuedJwtToken {

    private final String token;
    private final String jti;
    private final Instant expiresAt;
    private final List<String> roles;

    public IssuedJwtToken(String token, String jti, Instant expiresAt, List<String> roles) {
        this.token = token;
        this.jti = jti;
        this.expiresAt = expiresAt;
        this.roles = roles;
    }

    public String token() {
        return token;
    }

    public String jti() {
        return jti;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public List<String> roles() {
        return roles;
    }
}
