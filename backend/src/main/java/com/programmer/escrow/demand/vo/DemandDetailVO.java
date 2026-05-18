package com.programmer.escrow.demand.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandStagePlan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DemandDetailVO {

    private Long id;
    private String demandNo;
    private Long publisherId;
    private String publisherNickname;
    private String publisherAvatarUrl;
    private String publisherUserNo;
    private String publisherRealName;
    private String publisherPhone;
    private String publisherEmail;
    private String title;
    private String summary;
    private String detail;
    private Long categoryId;
    private String category;
    private Integer orderType;
    @JsonProperty("isUrgent")
    private Boolean urgent;
    private BigDecimal urgentBonus;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Integer expectedDays;
    private Integer deliveryType;
    private Integer quoteCount;
    private Integer reviewStatus;
    private Integer status;
    private String rejectReason;
    private String coverImage;
    private List<DemandFileItem> images;
    private List<DemandFileItem> attachments;
    private List<DemandStagePlan> stagePlans;
}
