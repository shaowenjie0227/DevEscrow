package com.programmer.escrow.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeveloperAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer approve;

    private String remark;
}
