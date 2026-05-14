package com.programmer.escrow.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserBanDTO {

    @NotBlank(message = "封禁原因不能为空")
    private String reason;

    private Integer days;
}
