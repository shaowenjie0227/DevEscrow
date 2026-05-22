package com.programmer.escrow.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiPromptTemplateStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
