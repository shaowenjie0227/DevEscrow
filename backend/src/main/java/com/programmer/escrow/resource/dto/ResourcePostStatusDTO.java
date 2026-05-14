package com.programmer.escrow.resource.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourcePostStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
