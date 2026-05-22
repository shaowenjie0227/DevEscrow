package com.programmer.escrow.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRuntimeConfig {

    private boolean enabled;

    private boolean fallbackEnabled;

    private String provider;

    private String baseUrl;

    private String chatPath;

    private String apiKey;

    private String model;

    private double temperature;

    private int topK;

    private long cacheTtlSeconds;

    private long connectTimeoutMs;

    private long readTimeoutMs;
}
