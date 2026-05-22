package com.programmer.escrow.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiPromptTemplateEntity {

    private Long id;

    private String sceneCode;

    private String sceneName;

    private String systemPrompt;

    private String userPromptTemplate;

    private String model;

    private Double temperature;

    private Integer topK;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
