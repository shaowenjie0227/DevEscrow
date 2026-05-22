package com.programmer.escrow.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiRuntimeConfigSaveDTO {

    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @NotBlank(message = "provider 不能为空")
    private String provider;

    @NotBlank(message = "baseUrl 不能为空")
    private String baseUrl;

    @NotBlank(message = "chatPath 不能为空")
    private String chatPath;

    private String apiKey;

    private Boolean clearApiKey;

    @NotBlank(message = "模型名不能为空")
    private String model;

    @NotNull(message = "temperature 不能为空")
    @Min(value = 0, message = "temperature 不能小于 0")
    @Max(value = 2, message = "temperature 不能大于 2")
    private Double temperature;

    @NotNull(message = "topK 不能为空")
    @Min(value = 1, message = "topK 至少为 1")
    @Max(value = 20, message = "topK 不能超过 20")
    private Integer topK;

    @NotNull(message = "fallbackEnabled 不能为空")
    private Boolean fallbackEnabled;

    @NotNull(message = "cacheTtlSeconds 不能为空")
    @Min(value = 0, message = "cacheTtlSeconds 不能小于 0")
    private Long cacheTtlSeconds;

    @NotNull(message = "connectTimeoutMs 不能为空")
    @Min(value = 1000, message = "connectTimeoutMs 不能小于 1000")
    private Long connectTimeoutMs;

    @NotNull(message = "readTimeoutMs 不能为空")
    @Min(value = 1000, message = "readTimeoutMs 不能小于 1000")
    private Long readTimeoutMs;
}
