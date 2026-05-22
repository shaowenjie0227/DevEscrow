package com.programmer.escrow.ai.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRuntimeConfigVO {

    private Long id;

    private String configKey;

    private Boolean enabled;

    private String provider;

    private String baseUrl;

    private String chatPath;

    private String maskedApiKey;

    private Boolean hasApiKey;

    private String model;

    private Double temperature;

    private Integer topK;

    private Boolean fallbackEnabled;

    private Long cacheTtlSeconds;

    private Long connectTimeoutMs;

    private Long readTimeoutMs;
}
