package com.programmer.escrow.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeveloperProfileVO {

    private Long userId;
    private String userNo;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String email;
    private String intro;
    private String realName;
    private String idCardNo;
    private Integer developerStatus;
    private Integer idVerifyStatus;
    private Integer skillAuditStatus;
    private String skillAuditReason;
    private Integer developerRoleType;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String selfieUrl;
    private List<Long> developerSkillTagIds;
}
