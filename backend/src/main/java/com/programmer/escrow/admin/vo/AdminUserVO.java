package com.programmer.escrow.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserVO {

    private Long id;
    private String userNo;
    private String phone;
    private String email;
    private String nickname;
    private Integer userType;
    private Integer developerStatus;
    private Integer idVerifyStatus;
    private Integer skillAuditStatus;
    private Integer developerRoleType;
    private String skillTags;
    private String realName;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String selfieUrl;
    private Integer status;
}
