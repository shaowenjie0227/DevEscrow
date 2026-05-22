package com.programmer.escrow.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiRuntimeConfigEntity {

    private Long id;

    private String configKey;

    private Integer enabled;

    private String provider;

    private String baseUrl;

    private String chatPath;

    private String apiKeyCiphertext;

    private String model;

    private Double temperature;

    private Integer topK;

    private Integer fallbackEnabled;

    private Long cacheTtlSeconds;

    private Long connectTimeoutMs;

    private Long readTimeoutMs;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
