package com.programmer.escrow.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class LoginVO {

    private String token;
    private String tokenType;
    private Instant expiresAt;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer userType;
    private Integer developerStatus;
    private Integer idVerifyStatus;
    private Integer skillAuditStatus;
    private Integer developerRoleType;
    private String skillTags;
    private List<String> roles;
    private String redirectPath;
}
