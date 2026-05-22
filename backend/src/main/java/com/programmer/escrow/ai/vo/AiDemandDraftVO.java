package com.programmer.escrow.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDemandDraftVO {

    private String title;

    private String summary;

    private String detail;

    private Long categoryId;

    private String categoryName;

    private Integer orderType;

    private Boolean urgent;

    private BigDecimal urgentBonus;

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    private Integer expectedDays;

    private Integer deliveryType;

    @Builder.Default
    private List<AiDemandDraftStageVO> stagePlans = new ArrayList<>();

    @Builder.Default
    private List<String> recommendedSkills = new ArrayList<>();

    @Builder.Default
    private List<String> riskTips = new ArrayList<>();

    @Builder.Default
    private List<AiDemandReferenceVO> references = new ArrayList<>();

    private Boolean fromCache;

    private Boolean fallbackUsed;

    private String promptVersion;
}
