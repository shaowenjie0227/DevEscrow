package com.programmer.escrow.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDemandDraftStageVO {

    private Integer stageNo;

    private String stageName;

    private String stageDesc;

    private BigDecimal stageAmount;
}
