package com.programmer.escrow.security.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class LoginUserCache {

    private Long userId;
    private Integer userType;
    private String nickname;
    private String avatarUrl;
    private List<String> roles;
    private String currentJti;
    private Instant expiresAt;
}
