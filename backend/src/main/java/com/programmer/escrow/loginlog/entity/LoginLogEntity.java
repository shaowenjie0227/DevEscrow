package com.programmer.escrow.loginlog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogEntity {

    private Long id;
    private Long userId;
    private String ip;
    private LocalDateTime loginTime;
    private String loginType;
}
