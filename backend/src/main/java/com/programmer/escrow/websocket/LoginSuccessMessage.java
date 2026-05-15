package com.programmer.escrow.websocket;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginSuccessMessage {

    private String type;
    private String token;
    private String jwt;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer userType;
    private List<String> roles;
    private String redirectPath;
}
