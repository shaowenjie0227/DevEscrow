package com.programmer.escrow.kb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeBaseStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
