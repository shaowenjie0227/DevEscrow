package com.programmer.escrow.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiCallLogCreateCommand {

    private String sceneCode;

    private String promptVersion;

    private String provider;

    private String model;

    private Long creatorUserId;

    private String requestDigest;

    private String requestPreview;

    private Integer contextCount;

    private Boolean cacheHit;

    private Boolean fallbackUsed;

    private Integer status;

    private Long latencyMs;

    private String errorMessage;

    private String resultPreview;
}
