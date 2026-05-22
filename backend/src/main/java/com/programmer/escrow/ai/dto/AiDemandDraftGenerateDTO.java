package com.programmer.escrow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDemandDraftGenerateDTO {

    @NotBlank(message = "请先输入希望 AI 帮你整理的需求描述")
    private String requirement;

    private String title;

    private String summary;

    private String detail;
}
