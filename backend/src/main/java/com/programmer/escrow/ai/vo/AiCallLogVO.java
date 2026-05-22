package com.programmer.escrow.ai.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiCallLogVO {

    private Long id;

    private String sceneCode;

    private String promptVersion;

    private String provider;

    private String model;

    private Long creatorUserId;

    private String requestPreview;

    private Integer contextCount;

    private Integer cacheHit;

    private Integer fallbackUsed;

    private Integer status;

    private Long latencyMs;

    private String errorMessage;

    private String resultPreview;

    private String createdAt;
}
