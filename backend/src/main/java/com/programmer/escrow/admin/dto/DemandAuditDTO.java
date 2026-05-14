package com.programmer.escrow.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemandAuditDTO {

    private Integer reviewStatus;

    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
