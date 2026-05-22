package com.programmer.escrow.ai.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiPromptTemplateVO {

    private Long id;

    private String sceneCode;

    private String sceneName;

    private String systemPrompt;

    private String userPromptTemplate;

    private String model;

    private Double temperature;

    private Integer topK;

    private Integer status;
}
