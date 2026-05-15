package com.programmer.escrow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeveloperApplyDTO {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号码不能为空")
    private String idCardNo;

    private Integer developerRoleType;

    @NotBlank(message = "身份证正面不能为空")
    private String idCardFrontUrl;

    @NotBlank(message = "身份证反面不能为空")
    private String idCardBackUrl;

    @NotBlank(message = "自拍照不能为空")
    private String selfieUrl;

    @NotEmpty(message = "技术栈不能为空")
    private List<Long> skillTagIds;
}
