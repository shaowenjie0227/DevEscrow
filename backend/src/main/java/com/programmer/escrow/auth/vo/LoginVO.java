package com.programmer.escrow.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;
    private List<String> roles;
}
