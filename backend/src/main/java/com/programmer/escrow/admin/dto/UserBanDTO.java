package com.programmer.escrow.admin.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserBanDTO {

    private String reason;

    @Min(value = 0, message = "封禁天数不能为负数")
    private Integer days;

    private Integer status;
}
