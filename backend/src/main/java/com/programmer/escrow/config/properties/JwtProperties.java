package com.programmer.escrow.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String issuer;
    private String secret;
    private long accessTokenTtlSeconds;
    private long refreshThresholdSeconds;
}
