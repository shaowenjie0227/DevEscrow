package com.programmer.escrow.user.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserEntity {

    private Long id;
    private String userNo;
    private String phone;
    private String email;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Integer userType;
    private String realName;
    private Integer idVerifyStatus;
    private Integer developerStatus;
    private String skillTags;
    private String intro;
    private BigDecimal rating;
    private Integer completedOrderCount;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
