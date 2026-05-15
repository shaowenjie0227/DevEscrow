package com.programmer.escrow.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginQrState {

    private QrLoginStatus status;
    private Instant createTime;
    private Instant scanTime;
    private Instant successTime;
    private Long userId;
    private String openid;
    private String jwt;
}
