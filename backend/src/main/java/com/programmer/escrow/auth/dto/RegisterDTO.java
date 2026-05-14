package com.programmer.escrow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RegisterDTO {

    private String phone;
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    // 开发者申请材料
    private String realName;
    private Integer developerRoleType;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String selfieUrl;
    @NotEmpty(message = "技术栈不能为空")
    private List<Long> skillTagIds;
}
