package com.programmer.escrow.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiContextDocument {

    private String sourceType;

    private Long sourceId;

    private String title;

    private String summary;

    private String extra;

    private String linkUrl;

    private double score;
}
