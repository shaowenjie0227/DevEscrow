package com.programmer.escrow.admin.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserEntity {

    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private String email;
    private String roleCode;
    private String permissionJson;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
