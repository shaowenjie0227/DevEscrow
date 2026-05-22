package com.programmer.escrow.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = false;

    private boolean fallbackEnabled = true;

    private String provider = "openai-compatible";

    private String baseUrl = "https://api.openai.com";

    private String chatPath = "/v1/chat/completions";

    private String apiKey;

    private String configCryptoKey;

    private String model = "gpt-4.1-mini";

    private double temperature = 0.2D;

    private int topK = 6;

    private long cacheTtlSeconds = 1800L;

    private long connectTimeoutMs = 3000L;

    private long readTimeoutMs = 20000L;
}
