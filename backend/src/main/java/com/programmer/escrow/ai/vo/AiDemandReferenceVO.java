package com.programmer.escrow.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDemandReferenceVO {

    private String sourceType;

    private Long sourceId;

    private String title;

    private String summary;

    private String extra;

    private String linkUrl;

    private Double score;
}
