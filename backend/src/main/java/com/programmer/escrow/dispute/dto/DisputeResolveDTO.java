package com.programmer.escrow.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisputeResolveDTO {

    @NotNull(message = "裁决结果不能为空")
    private Integer resultType;

    @NotBlank(message = "处理说明不能为空")
    private String resolutionNote;
}
