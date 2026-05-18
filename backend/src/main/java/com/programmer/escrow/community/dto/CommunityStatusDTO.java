package com.programmer.escrow.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommunityStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
