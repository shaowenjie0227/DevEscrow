package com.programmer.escrow.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiPromptTemplateSaveDTO {

    @NotBlank(message = "场景编码不能为空")
    private String sceneCode;

    @NotBlank(message = "场景名称不能为空")
    private String sceneName;

    private String systemPrompt;

    private String userPromptTemplate;

    private String model;

    @Min(value = 0, message = "temperature 不能小于 0")
    @Max(value = 2, message = "temperature 不能大于 2")
    private Double temperature;

    @NotNull(message = "topK 不能为空")
    @Min(value = 1, message = "topK 至少为 1")
    @Max(value = 20, message = "topK 不能超过 20")
    private Integer topK;
}
