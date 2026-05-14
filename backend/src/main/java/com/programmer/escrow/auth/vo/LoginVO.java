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
    private Integer userType;
    private Integer developerStatus;
    private Integer idVerifyStatus;
    private Integer skillAuditStatus;
    private Integer developerRoleType;
    private String skillTags;
    private List<String> roles;
}
