package com.programmer.escrow.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginVO {

    private String token;
    private Long adminId;
    private String username;
    private String realName;
    private String roleCode;
}
