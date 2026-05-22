package com.programmer.escrow.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiPromptRuntimeConfig {

    private String sceneCode;

    private String sceneName;

    private String promptVersion;

    private String systemPrompt;

    private String userPromptTemplate;

    private String model;

    private Double temperature;

    private Integer topK;

    private boolean custom;
}
