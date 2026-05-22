package com.programmer.escrow.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiCallLogEntity {

    private Long id;

    private String sceneCode;

    private String promptVersion;

    private String provider;

    private String model;

    private Long creatorUserId;

    private String requestDigest;

    private String requestPreview;

    private Integer contextCount;

    private Integer cacheHit;

    private Integer fallbackUsed;

    private Integer status;

    private Long latencyMs;

    private String errorMessage;

    private String resultPreview;

    private LocalDateTime createdAt;
}
